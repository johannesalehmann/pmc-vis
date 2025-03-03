FROM ubuntu:25.04
WORKDIR /usr/src/pmc-vis

### 1. node 20
RUN apt-get update && \
    apt-get install curl -y && \
    curl -sL https://deb.nodesource.com/setup_20.x | bash && \
    apt-get install nodejs -y && \
    node -v && npm -v

### 2. install app
COPY . ./
RUN npm install

EXPOSE 3000
ENTRYPOINT ["npm", "start"]
