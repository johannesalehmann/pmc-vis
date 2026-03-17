FROM movesrwth/storm:1.11.0

#Create working dir
RUN useradd -ms /bin/bash prismServer

#Load dependencies

ENV DEBIAN_FRONTEND=noninteractive
RUN apt update && apt install -y gcc-11 build-essential cmake openjdk-11-jdk git bc zsh maven curl ca-certificates libboost-program-options-dev rustup postgresql && rm -rf /var/lib/apt/lists/*
RUN install -d /usr/share/postgresql-common/pgdg
#RUN curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc
#RUN . /etc/os-release
#RUN sh -c "echo 'deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt questing-pgdg main' > /etc/apt/sources.list.d/pgdg.list"
#RUN apt update && apt install -y postgresql-14 && rm -rf /var/lib/apt/lists/*

ENV PATH="$PATH:/usr/lib/postgresql/17/bin"

RUN rustup default stable

#build prism

WORKDIR /home/prismServer

RUN git clone https://github.com/prismmodelchecker/prism prism
RUN git clone https://github.com/johannesalehmann/SVaBResp.git SVaBResp

#build prism
WORKDIR /home/prismServer/prism/prism

RUN git checkout 17a47f8

WORKDIR /home/prismServer/prism/prism

RUN make

#build SVaBResp
WORKDIR /home/prismServer/SVaBResp

#To lock to a certain commit
#RUN git checkout c541affb994f3ed044ae4e1dce3ed3dd078323be

RUN cargo build --release --package svabresp-cli --bin svabresp-cli

# Load Switts-Multi
WORKDIR /home/prismServer

COPY switss switss
WORKDIR /home/prismServer/switss/build

RUN cmake ..
RUN make

RUN cp switss-multi /usr/local/bin/

#Hook gurobi license
VOLUME ["/data"]
ENV GRB_LICENSE_FILE="/data/gurobi.lic"

#Load PMC-Vis backend
WORKDIR /home/prismServer

#Load everything into the image
COPY server/ server/

WORKDIR /home/prismServer/server

RUN mvn dependency:copy-dependencies

RUN mvn package

RUN chown -R prismServer . && chown -R prismServer /var/run/postgresql

RUN sed -i 's/\r$//' bin/run && chmod +x bin/run && chmod +x bin/initdb

USER prismServer

RUN /bin/bash -c 'bin/initdb'

ENTRYPOINT ["bin/run", "server", "DockerTools.yml"]

EXPOSE 8080
EXPOSE 8082
