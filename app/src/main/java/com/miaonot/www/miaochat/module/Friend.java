package com.miaonot.www.miaochat.module;

public class Friend {
    String id;
    String nickname;

    public Friend(String id, String nickname)
    {
        this.id = id;
        this.nickname = nickname;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
