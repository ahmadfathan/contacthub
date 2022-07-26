FROM ubuntu:18.04
RUN apt-get update -y && \
    apt-get install -y python3-pip python3-dev
RUN apt-get update -y
RUN DEBIAN_FRONTEND="noninteractive" apt-get -y install tzdata
RUN echo "Asia/Jakarta" > /etc/timezone
RUN rm -f /etc/localtime
RUN dpkg-reconfigure -f noninteractive tzdata
ADD . /app
WORKDIR /app
RUN pip3 install -r requirements.txt

# docker build --tag "api_contact_hub" .