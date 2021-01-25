FROM debian:buster-slim as node-base
LABEL author="Max Meinhold <mxmeinhold@gmail.com>"

# Yarn and nvm install deps
RUN rm /bin/sh \
    && ln -s /bin/bash /bin/sh \
    && apt-get update \
    && apt-get install -y curl \
    && apt-get -y autoclean

# NVM and node install
ENV NODE_VERSION 14.8.0

ENV NVM_DIR /usr/local/nvm
RUN mkdir $NVM_DIR \
    && curl --silent -o- https://raw.githubusercontent.com/creationix/nvm/v0.35.3/install.sh | bash

RUN source $NVM_DIR/nvm.sh \
    && nvm install $NODE_VERSION \ 
    && nvm alias default $NODE_VERSION \
    && nvm use default

ENV NODE_PATH $NVM_DIR/v$NODE_VERSION/lib/node_modules
ENV PATH $NVM_DIR/versions/node/v$NODE_VERSION/bin:$PATH

FROM node-base as builder

# Yarn install deps
RUN rm /bin/sh \
    && ln -s /bin/bash /bin/sh \
    && apt-get update \
    && apt-get install -y gnupg \
    && apt-get -y autoclean

WORKDIR /usr/src/deadass

# Yarn install
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add - \
    && echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list \
    && apt-get update \
    && apt-get install -y yarn \
    && apt-get -y autoclean

# Project dependencies
RUN mkdir ./backend && mkdir ./frontend
COPY package.json yarn.lock ./
COPY backend/package.json ./backend/
COPY frontend/package.json ./frontend/
RUN yarn install

# Build the bundle
COPY . .
RUN yarn workspace @csh/deadass-backend run build:production

FROM node-base as backend
WORKDIR /opt/deadass

# Wordlist - The american dictionary, removing apostrophes, short words, accent marks
RUN apt-get update \
    && apt-get install -y wamerican \
    && cat /usr/share/dict/words | grep -v "'" | awk '{ if (length($0) > 2) print tolower($0)}' | grep -Pv '[^\x00-\x7F]' | uniq > words.txt \
    && apt-get purge -y wamerican


COPY --from=builder /usr/src/deadass/backend/dist/bundle.js ./

EXPOSE 8080
USER 1001

CMD ["node", "bundle.js"]
