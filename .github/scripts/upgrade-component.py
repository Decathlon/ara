#!/usr/bin/env python

from ruamel.yaml import YAML
import os, sys
from argparse import ArgumentParser

yaml=YAML()

def upgrade_component_version(args):
    values_path = os.path.join(args.path, 'values.yaml')
    with open(values_path) as stream:
        loaded = yaml.load(stream)
    loaded[args.component]['image']['tag'] = args.version
    with open(values_path, 'wb') as stream:
        yaml.dump(loaded, stream)

parser = ArgumentParser(prog='upgrade-component')

parser.add_argument("path", help="chart path")
parser.add_argument("-c", "--component", help="component name under values", required=True)
parser.add_argument("-v", "--version", help="component version")

upgrade_component_version(parser.parse_args())
