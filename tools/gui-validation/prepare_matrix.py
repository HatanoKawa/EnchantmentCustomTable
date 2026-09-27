#!/usr/bin/env python3
"""Prepare isolated multi-version GUI worlds; never drive game menu business logic."""

import argparse
import json
from pathlib import Path
import re
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[2]
FULL = {'1.21.1', '1.21.2', '1.21.4', '1.21.5', '1.21.6', '1.21.9', '1.21.11'}
SWITCHES = ('enforceEnchantmentLevelLimit', 'incrementalSameLevelMerge',
            'convertOnlyLevelOneBook', 'freeConversionTableCosts')


def version_tuple(value):
    return tuple(map(int, value.split('.')))


def write_fixtures(version, destination):
    jar = Path.home() / '.gradle/caches/fabric-loom' / version / 'minecraft-client.jar'
    with zipfile.ZipFile(jar) as archive:
        info = json.loads(archive.read('version.json'))
    if info['id'] != version:
        raise ValueError('Minecraft metadata does not match requested version')
    shutil.copytree(ROOT / 'tools/gui-validation/fixtures', destination)
    pack = info['pack_version']
    metadata = {'description': 'ECT isolated GUI fixtures for Minecraft ' + version}
    if 'data_major' in pack:
        metadata.update(min_format=[pack['data_major'], pack['data_minor']],
                        max_format=[pack['data_major'], pack['data_minor']])
    else:
        metadata['pack_format'] = pack['data']
    (destination / 'pack.mcmeta').write_text(json.dumps({'pack': metadata}, indent=2) + '\n')
    for path in destination.rglob('*.mcfunction'):
        text = path.read_text()
        # The source world already disables daylight and mob spawning. Avoid renamed gamerules.
        text = ''.join(line for line in text.splitlines(keepends=True) if not line.startswith('gamerule '))
        if version_tuple(version) >= (1, 21, 5):
            text = re.sub(r'(minecraft:(?:stored_)?enchantments)=\{levels:(\{[^{}]*\})\}', r'\1=\2', text)
        path.write_text(text)
    return info


def prepare(loader, version, report, seed_world):
    matrix = ROOT / ('fabric_versions' if loader == 'fabric' else 'versions')
    if not (matrix / version / 'gradle.properties').is_file():
        raise ValueError('Version is not present in this loader matrix')
    run = matrix / version / 'run'
    world = run / 'saves' / ('ECT-GUI-R3-' + loader + '-' + version)
    target = report / loader / version
    if world.exists() or target.exists():
        raise FileExistsError('Already prepared; refusing to overwrite world or original-file backups')
    seed_name = seed_world.format(loader=loader, minecraft=version)
    if not seed_name or seed_name in {'.', '..'} or Path(seed_name).name != seed_name:
        raise ValueError('--seed-world must be a folder name in this target’s run/saves')
    seed = run / 'saves' / seed_name
    if seed.resolve().parent != (run / 'saves').resolve():
        raise ValueError('Seed must remain in this target’s run/saves')
    if not (seed / 'level.dat').is_file():
        raise FileNotFoundError('Create a same-version dedicated test world first: ' + str(seed))
    shutil.copytree(seed, world)
    # Remove only the inherited test pack in this newly created world.
    fixture = world / 'datapacks/ectgui'
    if fixture.exists():
        shutil.rmtree(fixture)
    info = write_fixtures(version, fixture)
    target.mkdir(parents=True)
    config_name = 'enchantment_custom_table.json' if loader == 'fabric' else 'enchantment_custom_table-common.toml'
    paths = {'config': run / 'config' / config_name, 'options': run / 'options.txt'}
    originals = {}
    for label, path in paths.items():
        originals[label] = {'path': str(path.relative_to(ROOT)), 'existed': path.exists()}
        if path.exists():
            shutil.copy2(path, target / (label + '-original'))
        path.parent.mkdir(parents=True, exist_ok=True)
    default = ROOT / 'tools/gui-validation/evidence/round2-2026-09-22' / (
        loader + '-config-default.' + ('json' if loader == 'fabric' else 'toml'))
    shutil.copy2(default, paths['config'])
    # Keep this target's options; never copy settings or worlds from another version.
    entry = {'loader': loader, 'minecraft': version, 'tier': 'full' if version in FULL else 'quick',
             'world': str(world.relative_to(ROOT)), 'seed': str(seed.relative_to(ROOT)), 'game_metadata': info,
             'originals': originals, 'status': 'prepared', 'cases': {}}
    (target / 'state.json').write_text(json.dumps(entry, indent=2) + '\n')
    shutil.copy2(paths['config'], target / 'config-default')
    print('Prepared', loader, version, entry['tier'], world.name)


def configure(loader, version, report, mode):
    target = report / loader / version
    state = json.loads((target / 'state.json').read_text())
    config = ROOT / state['originals']['config']['path']
    if mode == 'restore':
        for label, original in state['originals'].items():
            path = ROOT / original['path']
            if original['existed']:
                shutil.copy2(target / (label + '-original'), path)
                assert path.read_bytes() == (target / (label + '-original')).read_bytes()
            else:
                path.unlink(missing_ok=True)
        state['restored'] = True
    else:
        state['restored'] = False
        text = (target / 'config-default').read_text()
        if mode == 'strict-free':
            if loader == 'fabric':
                data = json.loads(text)
                for name in SWITCHES:
                    data[name] = True
                text = json.dumps(data, indent=2) + '\n'
            else:
                for name in SWITCHES:
                    text = text.replace(name + ' = false', name + ' = true')
        config.write_text(text)
        (target / ('config-' + mode)).write_text(text)
    (target / 'state.json').write_text(json.dumps(state, indent=2) + '\n')
    print(mode, loader, version)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--loader', choices=['fabric', 'neoforge', 'both'], default='both')
    parser.add_argument('--minecraft', default='all')
    parser.add_argument('--seed-world', help='Same-version seed folder in each target’s run/saves; supports {loader} and {minecraft}')
    parser.add_argument('--report', type=Path, default=ROOT / 'build/reports/gui-validation/round3')
    parser.add_argument('--config', choices=['default', 'strict-free', 'restore'],
                        help='Only change/restore configuration; requires stopped client')
    args = parser.parse_args()
    if not args.config and not args.seed_world:
        parser.error('--seed-world is required when preparing a world')
    versions = sorted((p.parent.name for p in (ROOT / 'versions').glob('*/gradle.properties')), key=version_tuple)
    if args.minecraft != 'all':
        versions = [args.minecraft]
    for loader in (['fabric', 'neoforge'] if args.loader == 'both' else [args.loader]):
        for version in versions:
            if args.config:
                configure(loader, version, args.report, args.config)
            else:
                prepare(loader, version, args.report, args.seed_world)


if __name__ == '__main__':
    main()
