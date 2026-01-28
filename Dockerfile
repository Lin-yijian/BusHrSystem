FROM ycr.yonyoucloud.com/base/nginx:1.15-alpine

ADD . /app

RUN mkdir -p /yonyoucloud-buildproduct 

RUN cd /app && touch /yonyoucloud-buildproduct/README.md
