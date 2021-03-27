from setuptools import setup, find_packages

requires = [
    "colorlog>=3.1.4",
    "Cython",
    "dataclasses-avroschema",
    "faust",
    "multidict<5.0,>=4.5",
    "numpy",
    "python-schema-registry-client",
    "scikit-multiflow",
    "simple-settings",
    "typing-extensions",
    "yarl<1.6.0,>=1.0"
]

setup(
    name='pg-streaming-machine-learning',
    version='0.0.1',
    description='Machine learning demo on a stream of hopping windows',
    long_description='Example running Scikit-Multiflow with Faust, Avro and Schema Registry, and Docker Compose',
    classifiers=[
        "Programming Language :: Python",
    ],
    author='Johannes Kross',
    author_email='kross@fortiss.org',
    url='https://github.com/perguard/',
    packages=find_packages(),
    include_package_data=True,
    zip_safe=False,
    install_requires=requires,
    tests_require=[],
    setup_requires=[],
    dependency_links=[],
    entry_points={
        'console_scripts': [
            'pg_streaming_machine_learning = pg_streaming_machine_learning.app:main',
        ]
    },
)
