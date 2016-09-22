#!/usr/bin/env python2.7
from setuptools import setup

'''
import codecs
import os
import re


def find_version(fpath):
    here = os.path.abspath(os.path.dirname(__file__))
    with codecs.open(os.path.join(here, fpath), 'r') as f:
        version_file = f.read()
    matched = re.search(
        r"^__version__\s+=\s+['\"]([^'\"]*)['\"]", version_file, re.M)
    if matched:
        return matched.group(1)
    raise RuntimeError('version string undefined')
'''

setup(
    name="backend",
    version='1.0',
    description="Python Distribution backend installation for PG and flask",
    url="http://github.com/nidhinarendra"
    author_email='nidhi.narendra1@gmail.com',
    install_requires=[
        'flask==0.10.1',
        'geopy==1.11.0',
        'ipdb==0.9.0',
        'ipython==4.1.2',
        'ipython-genutils==0.1.0',
        'psycopg2==2.6.1',
        'names==0.3.0',
        'requests==2.9.1',
        'path.py==8.1.2',
        'pycrypto==2.6.1',
        'Werkzeug==0.11.4',
        'ujson==1.33',
        'jwt==0.3.2',
        'SQLAlchemy==1.0.12',
        'psycopg2==2.6.1',
        'nose>=1.3.7',
        'mock>=1.3.0',
        'rednose>=0.4.3',
        'coverage==4.0.3'
        #'pyyaml>=3.11,<4.0',
        #'six>=1.10.0',
    ]
)
