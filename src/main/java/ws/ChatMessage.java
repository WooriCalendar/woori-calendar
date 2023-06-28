package ws;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ChatMessage {

  private String sendEmail;
  private String revEmail;
  private String comment;
  private String type;
  private Date sdate;
  private Date rdate;
  private Long calNo;
}
