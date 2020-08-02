FROM node:14-buster-slim
LABEL maintainer="Max Meinhold <mxmeinhold@gmail.com>"

EXPOSE 8080

RUN mkdir /opt/deadass
WORKDIR /opt/deadass

COPY package.json yarn.lock ./

RUN yarn install --production=true

COPY . /opt/deadass

USER 1001

CMD ["node"]
