package com.example.videocalling.Listeners;

import com.example.videocalling.Models.User;

public interface UserListener {
    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);
}
