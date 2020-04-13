FROM node:lts-slim
WORKDIR /app
COPY ./react-front-end .
RUN npm install --slient

COPY ./resources .

EXPOSE 3000
CMD [ "npm", "start" ]
