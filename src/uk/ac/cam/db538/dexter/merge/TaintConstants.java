package uk.ac.cam.db538.dexter.merge;

public class TaintConstants {

  public static final int TAINT_SOURCE_CONTACTS = 	1 << 0;
  public static final int TAINT_SOURCE_SMS = 		1 << 1;
  public static final int TAINT_SOURCE_CALL_LOG = 	1 << 2;
  public static final int TAINT_SOURCE_LOCATION = 	1 << 3;
  public static final int TAINT_SOURCE_BROWSER = 	1 << 4;
  public static final int TAINT_SOURCE_DEVICE_ID = 	1 << 5;

  public static final int TAINT_SINK_NET = 			1 << 30;
  public static final int TAINT_SINK_OUT = 			1 << 31;

  public static final void init() {
    ObjectTaintStorage.set(System.out, TAINT_SINK_OUT);
  }

  public static final int queryTaint(String query) {
    if (query.startsWith("content://com.android.contacts"))
      return TAINT_SOURCE_CONTACTS;
    else if (query.startsWith("content://sms"))
      return TAINT_SOURCE_SMS;
    else if (query.startsWith("content://call_log"))
      return TAINT_SOURCE_CALL_LOG;
    return 0;
  }

  public static final int serviceTaint(String name) {
    if (name.equals("location"))
      return TAINT_SOURCE_LOCATION;
    else if (name.equals("phone"))
      return TAINT_SOURCE_DEVICE_ID;
    return 0;
  }
}
