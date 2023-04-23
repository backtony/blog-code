> nginx 설정 설명 : https://deeplify.dev/server/web/nginx-configuration
> nginx 설정 주의사항 : https://nginxstore.com/blog/nginx/%EA%B0%80%EC%9E%A5-%EB%A7%8E%EC%9D%B4-%EC%8B%A4%EC%88%98%ED%95%98%EB%8A%94-nginx-%EC%84%A4%EC%A0%95-%EC%97%90%EB%9F%AC-10%EA%B0%80%EC%A7%80/
cors

## mac nginx 설치
> https://velog.io/@davelee/mac%EC%97%90-nginx-%EC%84%A4%EC%B9%98%ED%95%98%EA%B8%B0

```sh
# 설치
brew install nginx

# 설정 파일 위치 확인
brew info nginx

## 출력 결과
Docroot is: /opt/homebrew/var/www

The default port has been set in /opt/homebrew/etc/nginx/nginx.conf to 8080 so that
nginx can run without sudo.

nginx will load all files in /opt/homebrew/etc/nginx/servers/.

To restart nginx after an upgrade:
  brew services restart nginx
Or, if you don't want/need a background service you can just run:
  /opt/homebrew/opt/nginx/bin/nginx -g daemon off;
==> Analytics
install: 1,407 (30 days), 42,290 (90 days), 387,031 (365 days)
install-on-request: 1,406
```
`/opt/homebrew/etc/nginx/nginx.conf` 에 설정파일이 위치한 것을 확인할 수 있다.

```sh
# nginx 시작 
nginx

# nginx stop
nginx -s stop

# 데몬 말고 계속 보려면
nginx -g "daemon off;"
```