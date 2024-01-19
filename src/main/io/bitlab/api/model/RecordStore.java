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
  private static final String FILE_PATH=System.getProperty("user.home")+"/.klondike/";
  private static final String FILE_NAME="rms_klondike";
  private static final File _FILE=new File(FILE_PATH+FILE_NAME);
  private static int[] data=null;

  public static RecordStore openRecordStore() {
    try(FileInputStream fis=new FileInputStream(_FILE);
        ObjectInputStream ois=new ObjectInputStream(fis)) {
      data=(int[])ois.readObject();
    } catch(FileNotFoundException e) {
      new File(FILE_PATH).mkdir();
      data=new int[]{0,0,0,0,0,0,0,0,0,0};
    } catch(Exception e) {
      e.printStackTrace();
    }
    return new RecordStore();
  }

  public static int[] getRecord() {
    return data;
  }

  public void setRecord(int[] data) {
    try(FileOutputStream fos=new FileOutputStream(_FILE);
        ObjectOutputStream oos=new ObjectOutputStream(fos)) {
      oos.writeObject(data);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
