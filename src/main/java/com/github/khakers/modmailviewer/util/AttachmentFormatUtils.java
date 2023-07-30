package com.github.khakers.modmailviewer.util;

import com.github.khakers.modmailviewer.data.Attachment;

public class AttachmentFormatUtils {

    public static boolean isMediaAttachment(Attachment attachment) {
        return isImage(attachment) || isVideo(attachment);
    }

    public static boolean isImage(Attachment attachment) {
        if (attachment.contentType() == null) {
            return isImage(getFileExtension(attachment.filename()));
        }
        return attachment.contentType().startsWith("image");
    }

    public static boolean isVideo(Attachment attachment) {
        if (attachment.contentType() == null) {
            return isSupportedVideoContainer(getFileExtension(attachment.filename()));
        }
        return attachment.contentType().startsWith("video");
    }

    public static boolean isAudio(Attachment attachment) {
        if (attachment.contentType() == null) {
            return isAudio(getFileExtension(attachment.filename()));
        }
        return attachment.contentType().startsWith("audio");
    }

    public static boolean isImage(String format) {
        format = format
              .strip()
              .replace(".", "")
              .toLowerCase();
        return format.equals("png")
              || format.equals("jpg")
              || format.equals("jpeg")
              || format.equals("gif")
              || format.equals("webp")
              || format.equals("bmp")
              || format.equals("tiff")
              || format.equals("avif");
    }


    /**
     * Best effort guess at whether the file is an audio file based on the file extension
     *
     * @param format The file extension string
     * @return Whether the file extension appears to be an audio file
     */
    public static boolean isAudio(String format) {
        format = format
              .strip()
              .replace(".", "")
              .toLowerCase();
        return format.equals("ogg")
              || format.equals("oga")
              || format.equals("wav")
              || format.equals("flac")
              || format.equals("mp3")
              || format.equals("opus")
              || format.equals("pcm")
              || format.equals("vorbis")
              || format.equals("aac");
    }

    public static boolean isSupportedAudioContainer(String format) {
        format = format.toLowerCase();
        return format.equals("ogg")
              || format.equals("wav")
              || format.equals("flac");
    }

    /**
     * Best effort guess at whether the file is a supported video container in chromium based on the file extension
     *
     * @param format The file extension string
     * @return Whether the file extension appears to be supported video container
     */
    public static boolean isSupportedVideoContainer(String format) {
        format = format
              .strip()
              .replace(".", "")
              .toLowerCase();
        return format.equals("webm")
              || format.equals("mp4")
              || format.equals("mkv");
    }

    public static String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
