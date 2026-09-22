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
    parser.add_argument('--launch', action='store_true', help='Run the prepared client in this process')
    args = parser.parse_args()
    if sys.platform != 'darwin':
        parser.error('This experiment requires macOS.')

    root = Path(__file__).resolve().parents[2]
    build = root / 'versions/1.21.1/build/gui-validation'
    manifest = build / 'launch.json'
    if not manifest.is_file():
        parser.error('Run Gradle with export-client.init.gradle and :1.21.1:exportGuiClientLaunch first.')
    launch = json.loads(manifest.read_text())
    java_home = Path(launch['javaHome'])
    java = Path(launch['command'][0])
    libjli = java_home / 'lib/libjli.dylib'
    if not java.is_file() or not libjli.is_file():
        parser.error('The exported Java toolchain is no longer available; export again.')

    app = build / 'ECT GUI Dev.app'
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
            'CFBundleIdentifier': 'com.river-quinn.ect.gui-dev.mc1211',
            'CFBundleName': 'ECT GUI Dev',
            'CFBundleDisplayName': 'ECT GUI Dev',
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
        os.execve(executable, [str(executable), *launch['command'][1:]], environment)


if __name__ == '__main__':
    main()
