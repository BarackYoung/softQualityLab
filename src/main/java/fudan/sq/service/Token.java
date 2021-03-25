package fudan.se.lab2.service;

public class Token {
    String token;
    String expireTime;
    public Token(){

    }
    public void setToken(String token){
        this.token = token;
    }
    public String getToken(){
        return token;
    }
}
