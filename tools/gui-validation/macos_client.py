#!/usr/bin/env python3
"""Package the existing Java launcher as a local macOS app and run the dev client."""

import argparse
import json
import os
from pathlib import Path
import plistlib
import shutil
import subprocess
import sys


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--loader', choices=['neoforge', 'forge', 'fabric'], default='neoforge')
    parser.add_argument('--minecraft', default='1.21.1', help='Minecraft version project to launch')
    parser.add_argument('--legacy', action='store_true', help='Use the independent Forge/Fabric project layout')
    parser.add_argument('--launch', action='store_true', help='Run the prepared client in this process')
    parser.add_argument('--keep-awake', action='store_true',
                        help='Keep display/system awake for this client, at most one hour')
    args = parser.parse_args()
    if sys.platform != 'darwin':
        parser.error('This experiment requires macOS.')
    if args.keep_awake and not args.launch:
        parser.error('--keep-awake requires --launch.')

    root = Path(__file__).resolve().parents[2]
    if args.legacy:
        if args.loader not in ('forge', 'fabric'):
            parser.error('Legacy projects use Forge or Fabric.')
        project = root / args.loader
    else:
        matrix = root / ('fabric_versions' if args.loader == 'fabric' else 'versions')
        if args.minecraft not in {p.name for p in matrix.iterdir() if (p / 'gradle.properties').is_file()}:
            parser.error('Minecraft version is not present in this loader matrix.')
        project = matrix / args.minecraft
    build = root / project / 'build/gui-validation'
    manifest = build / 'launch.json'
    if not manifest.is_file():
        parser.error('Export this loader with export-client.init.gradle and exportGuiClientLaunch first.')
    launch = json.loads(manifest.read_text())
    # Older Loom RunConfig providers can append this macOS flag on each evaluation.
    # Keep one first-thread request in the exported launcher command.
    first_thread_seen = False
    command = []
    for argument in launch['command']:
        if argument == '-XstartOnFirstThread':
            if first_thread_seen:
                continue
            first_thread_seen = True
        command.append(argument)
    launch['command'] = command
    if launch['loader'] != args.loader or launch.get('minecraftVersion', '1.21.1') != args.minecraft:
        parser.error('Launch manifest does not match the requested loader/version; export again.')
    java_home = Path(launch['javaHome'])
    java = Path(launch['command'][0])
    libjli = java_home / 'lib/libjli.dylib'
    if not java.is_file() or not libjli.is_file():
        parser.error('The exported Java toolchain is no longer available; export again.')

    app_name = {'fabric': 'ECT GUI Fabric', 'forge': 'ECT GUI Forge', 'neoforge': 'ECT GUI Dev'}[args.loader]
    bundle_id = ('com.river-quinn.ect.gui-dev.fabric.mc1211'
                 if args.loader == 'fabric' else 'com.river-quinn.ect.gui-dev.mc1211')
    if args.minecraft != '1.21.1':
        app_name += ' ' + args.minecraft
        bundle_id = 'com.river-quinn.ect.gui-dev.' + args.loader + '.mc' + args.minecraft.replace('.', '-')
    app = build / f'{app_name}.app'
    contents = app / 'Contents'
    macos = contents / 'MacOS'
    macos.mkdir(parents=True, exist_ok=True)
    executable = macos / 'ECTGuiDev'
    # Copy the native launcher itself: a shell wrapper would leave an unidentified Java child.
    shutil.copy2(java, executable)
    library_link = macos / 'libjli.dylib'
    if library_link.is_symlink():
        library_link.unlink()
    elif library_link.exists():
        parser.error(f'Refusing to overwrite unexpected file: {library_link}')
    library_link.symlink_to(libjli)
    with (contents / 'Info.plist').open('wb') as stream:
        plistlib.dump({
            'CFBundleExecutable': 'ECTGuiDev',
            # Keep the original NeoForge identity stable for macOS/automation app caches.
            'CFBundleIdentifier': bundle_id,
            'CFBundleName': app_name,
            'CFBundleDisplayName': app_name,
            'CFBundlePackageType': 'APPL',
            'CFBundleShortVersionString': '1.0',
            'CFBundleVersion': '1',
            'NSHighResolutionCapable': True,
        }, stream)

    # The copied executable's old signature does not describe our new Info.plist.
    # Sign only the local bundle; never modify the source JDK or sign through its symlink.
    subprocess.run(['/usr/bin/codesign', '--force', '--sign', '-', str(app)], check=True)
    subprocess.run(['/usr/bin/codesign', '--verify', str(app)], check=True)
    # Before launching Minecraft, prove the relocated binary still finds its original JDK.
    subprocess.run([str(executable), '-version'], check=True)
    print(f'Prepared: {app}', flush=True)
    if args.launch:
        working_directory = Path(launch['workingDirectory'])
        working_directory.mkdir(parents=True, exist_ok=True)
        os.chdir(working_directory)
        environment = os.environ.copy()
        environment.update({key: str(value) for key, value in launch['environment'].items()})
        if args.keep_awake:
            # execve retains this PID; assertions end when the game exits or the timeout expires.
            subprocess.Popen(['/usr/bin/caffeinate', '-d', '-i', '-u', '-t', '3600',
                              '-w', str(os.getpid())])
        os.execve(executable, [str(executable), *launch['command'][1:]], environment)


if __name__ == '__main__':
    main()
