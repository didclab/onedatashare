FROM node:slim

WORKDIR /app
RUN mkdir react-front-end

COPY ./react-front-end/package*.json react-front-end/

RUN mkdir resources
ADD ./resources resources/
ADD ./react-front-end react-front-end/

WORKDIR /app/react-front-end

RUN npm install --silent
CMD npm start
EXPOSE 3000
