/**
 * @author JohnnyTB (jtrejosb@live.com)
 * gitlab.com/rootpass | github.com/rootpasss
 *
 * Licenses GNU GPL v3.0 and Eclipse Public License 2.0
 * Date: 12/09/2023, Time: 16:11:11
 */
package io.bitlab.api.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RecordStore {
  private static final String FILE_PATH=System.getProperty("user.home")+"/.klondike";
  private static final String FILE_NAME="/rms_klondike";
  private static final File _FILE=new File(FILE_PATH+FILE_NAME);
  private static byte[] bytes=null;

  public static void openRecordStore() {
    try(FileInputStream fis=new FileInputStream(_FILE);
        ObjectInputStream ois=new ObjectInputStream(fis)) {
      bytes=(byte[])ois.readObject();
    } catch(FileNotFoundException e) {
      new File(FILE_PATH).mkdir();
      bytes=toByteArray(new int[]{0,0,0});
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public static byte[] getRecord() {
    return bytes;
  }

  public void setRecord(int[] data) {
    try(FileOutputStream fos=new FileOutputStream(_FILE);
        ObjectOutputStream oos=new ObjectOutputStream(fos)) {
      oos.writeObject(toByteArray(data));
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private static byte[] toByteArray(int[] data) {
    byte[]bytes=new byte[data.length];
    for(int i=0;i<data.length;i++)
      bytes[i]=(byte)data[i];
    return bytes;
  }
}
