FROM node:20-alpine AS node
WORKDIR /web

COPY . ./
RUN npm install 

# && npm run build
# FROM nginx
# COPY --from=node /web/dist /usr/share/nginx/html
# RUN rm /usr/share/nginx/html/50x.html

EXPOSE 5173
ENTRYPOINT ["npm", "run", "dev", "--", "--host"]