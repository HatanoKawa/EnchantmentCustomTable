#!/usr/bin/env python3
"""Prepare same-version isolated worlds and reversible settings for Shift GUI tests."""

import argparse
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import time

from prepare_matrix import ROOT, SWITCHES, write_fixtures


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--checkout', type=Path, default=ROOT)
    parser.add_argument('--loader', choices=['fabric', 'neoforge', 'forge'], required=True)
    parser.add_argument('--minecraft', required=True)
    parser.add_argument('--legacy', action='store_true')
    parser.add_argument('--config', choices=['default', 'strict', 'restore'])
    args = parser.parse_args()
    root = args.checkout.resolve()
    version, loader = args.minecraft, args.loader
    project = root / loader if args.legacy else root / ('fabric_versions' if loader == 'fabric' else 'versions') / version
    run = project / 'run'
    if not (project / 'build/gui-validation/launch.json').is_file():
        parser.error('Export the matching client first.')
    processes = subprocess.check_output(['ps', '-axo', 'comm='], text=True)
    if any('Contents/MacOS/ECTGuiDev' in line for line in processes.splitlines()):
        parser.error('Stop the current test client before preparing or changing configuration.')
    report = root / 'build/reports/gui-validation/shift-matrix-2026-09-26' / loader / version
    state_file = report / 'state.json'
    if args.config:
        state = json.loads(state_file.read_text())
    else:
        world = run / 'saves' / ('ECT-SHIFT-R5-' + loader + '-' + version)
        seed_name = ('ECT-GUI-R4-' + loader + '-' + version.replace('.', '_') if args.legacy or version == '26.3'
                     else 'ECT-GUI-R3-' + loader + '-' + version)
        seed = run / 'saves' / seed_name
        if world.exists() or report.exists():
            parser.error('Refusing to overwrite an existing world or backup.')
        if not (seed / 'level.dat').is_file():
            parser.error('Missing same-version test seed: ' + str(seed))
        report.mkdir(parents=True)
        paths = {'config': run / 'config' / ('enchantment_custom_table.json' if loader == 'fabric'
                                           else 'enchantment_custom_table-common.toml'),
                 'options': run / 'options.txt'}
        originals = {}
        for label, path in paths.items():
            originals[label] = {'path': str(path.relative_to(root)), 'existed': path.exists()}
            if path.exists():
                shutil.copy2(path, report / (label + '-original'))
        state = {'loader': loader, 'minecraft': version, 'legacy': args.legacy,
                 'code_base': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=root, text=True).strip(),
                 'world': str(world.relative_to(root)), 'originals': originals,
                 'started_at_unix': time.time(), 'restored': False, 'cases': {}}
        state_file.write_text(json.dumps(state, indent=2) + '\n')
        shutil.copytree(seed, world)
        fixture = world / 'datapacks/ectgui'
        if fixture.exists():
            shutil.rmtree(fixture)  # Only the inherited fixture in this newly copied test world.
        if args.legacy:
            shutil.copytree(root / 'tools/gui-validation/fixtures', fixture)
            for namespace, name in [('ectgui', 'shiftcontrol'), ('minecraft', 'ectguishiftcontrol')]:
                shutil.copy2(ROOT / f'tools/gui-validation/fixtures/data/{namespace}/function/{name}.mcfunction',
                             fixture / f'data/{namespace}/functions/{name}.mcfunction')
        else:
            write_fixtures(version, fixture)
        options = paths['options'].read_text() if paths['options'].exists() else ''
        values = {'guiScale': '2', 'renderDistance': '4', 'simulationDistance': '4',
                  'pauseOnLostFocus': 'false', 'fullscreen': 'false'}
        lines = [line for line in options.splitlines() if line.split(':', 1)[0] not in values]
        paths['options'].write_text('\n'.join(lines + [key + ':' + value for key, value in values.items()]) + '\n')
    if args.config == 'restore':
        for label, entry in state['originals'].items():
            path = root / entry['path']
            if entry['existed']:
                shutil.copy2(report / (label + '-original'), path)
                assert path.read_bytes() == (report / (label + '-original')).read_bytes()
                entry['restored_sha256'] = hashlib.sha256(path.read_bytes()).hexdigest()
            else:
                path.unlink(missing_ok=True)
                assert not path.exists()
        state['restored'] = True
    else:
        values = {'minimumEmeraldCost': 36, 'minimumEmeraldBlockCost': 4,
                  **{key: False for key in SWITCHES}}
        if args.config == 'strict':
            values.update(enforceEnchantmentLevelLimit=True, incrementalSameLevelMerge=True)
        text = (json.dumps(values, indent=2) + '\n' if loader == 'fabric' else
                '\n'.join(key + ' = ' + json.dumps(value) for key, value in values.items()) + '\n')
        config = root / state['originals']['config']['path']
        config.parent.mkdir(parents=True, exist_ok=True)
        config.write_text(text)
        (report / ('config-' + (args.config or 'default'))).write_text(text)
        state['restored'] = False
    state_file.write_text(json.dumps(state, indent=2) + '\n')
    print(loader, version, args.config or 'prepared', state['world'])


if __name__ == '__main__':
    main()
