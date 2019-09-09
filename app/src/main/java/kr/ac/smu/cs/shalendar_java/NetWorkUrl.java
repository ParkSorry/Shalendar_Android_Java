package kr.ac.smu.cs.shalendar_java;
/*
  서버 URL이 매일 바뀌니
  여기서 toServerUrl 멤버변수만 바꾸면 됨
  각 Activity에서 통신 할 때는 getServerUrl()호출하여 해당 경로 더 붙여줘야

  ex)
  CreateMember(회원 가입) Activity에서는
  통신 URL :: getServerUrl() + /signup
 */
public class NetWorkUrl {

    private String toServer_URL = "https://54d6c78f.ngrok.io/MIND";

    public String getServerUrl() {
        return this.toServer_URL;
    }
}
