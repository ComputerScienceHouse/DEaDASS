FROM mcr.microsoft.com/vscode/devcontainers/universal:1-linux

# ** [Optional] Uncomment this section to install additional packages. **
USER root

RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends wamerican \
    && cat /usr/share/dict/words | grep -v "'" | awk '{ if (length($0) > 2) print tolower($0)}' | grep -Pv '[^\x00-\x7F]' | uniq > words.txt \
    && apt-get purge -y wamerican

USER codespace
