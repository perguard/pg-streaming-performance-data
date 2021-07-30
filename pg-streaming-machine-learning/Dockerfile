FROM python:3.7-slim

RUN echo 'sudo aptitude install debian-archive-keyring'

RUN echo 'apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 8B48AD6246925553'\
    echo 'deb [check-valid-until=no] http://archive.debian.org/debian jessie-backports main' >> /etc/apt/sources.list
     
    
RUN echo 'apt-get update' \
    echo 'apt-get install -y --no-install-recommends apt-utils' 


ENV PIP_FORMAT=legacy
ENV PIP_DISABLE_PIP_VERSION_CHECK=1

RUN echo 'apt-get install -y netcat && apt-get autoremove -y'

RUN adduser --disabled-password --gecos '' pg

WORKDIR /.

COPY . /.

RUN pip3 install -e .

CMD ["./run.sh"]
