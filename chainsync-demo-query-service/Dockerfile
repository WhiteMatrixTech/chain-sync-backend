FROM openjdk:11
VOLUME /tmp
ADD build/libs/chainsync-demo-query-service.jar application.jar
COPY bin/docker-start.sh /bin/docker-start.sh
RUN chmod +x /bin/docker-start.sh
CMD ["/bin/docker-start.sh"]
EXPOSE 8080
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \&& echo 'Asia/Shanghai' >/etc/timezone
