# STAGE 1 : build
FROM node:12 as builder

WORKDIR /usr/src/app

COPY package.json .

RUN npm install

COPY . ./

RUN npm run build

# STAGE 2 : serve

FROM nginx

COPY --from=builder /usr/src/app/dist /usr/share/nginx/html
COPY config/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
