package com.pedestrianassistant.Service.Media;

import com.pedestrianassistant.Model.Media.Photo.Photo;
import com.pedestrianassistant.Model.Media.Video.Video;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaServiceResult {
    private List<Photo> photos;
    private List<Video> videos;
}
