package de.oveits.velocitytemple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.camel.Header;
import org.apache.camel.util.FileUtil;

public class FileUtilBeans {		
	public final boolean deleteFile(@Header("fileName") final String fileName)  {
//			if (true) throw(new RuntimeException("FileUtilBeans.deleteFile: exit1"));			

	        File file = new File(fileName);
//			if (true) throw(new RuntimeException("FileUtilBeans.deleteFile: exit2"));	
	        
	        boolean returnValue = FileUtil.deleteFile(file);

	        return returnValue; //FileUtil.deleteFile(file);
	    }

	public final String listFiles(@Header("directoryName") final String directoryName)  {

        File directory = new File(directoryName);
        File[] filesArray = directory.listFiles();
        String body = "";
        
     // for each pathname in pathname array
        for (File file:filesArray) {
           // prints file and directory paths
//           System.out.println(file);
           body = body + file.getPath() + "\n";
//           System.out.println("body=" + body);
//   		if (true) throw(new RuntimeException("listFiles: exit2"));			

        }
        
        return body;
    }


	// not needed: instead I use .to("file:...") directive in Camel
//	public boolean createFile(@Header("fileName") String fileName) throws IOException  {
//        File file = new File(fileName);
//        return FileUtil.createNewFile(file);
//    }


	public final byte[] readFile(@Header("folderList") final String folderList, @Header("fileName") final String fileName) { //, @Body byte[] body){
		// folderList accepts a comma-separated list of folders
		if (folderList == null) {
			throw(new RuntimeException("FileUtilBeans.class: readFile was called with null FolderList!"));			
		}
		
		String[] fullPathArray = folderList.split(",");
		byte[] body = null;

		// loop over list of folders:
		for (int i = 0; i < fullPathArray.length; i++) {
			try {

				String name = fullPathArray[i].trim() + "/" + fileName;
				File inputFile = new File(name);

				FileInputStream fileInputStream = new FileInputStream(
						inputFile);
				byte[] buffer = new byte[(int) inputFile
						.length()];

				try {
					
					fileInputStream.read(buffer);
					body = buffer;
				} finally {
					fileInputStream.close();
				}
				// if you reach here, the file was found and we
				// can leave the for loop:
				break;
			} catch (IOException e) {
				// Throw an error, if this was the last file
				// path in the list
				if (i == fullPathArray.length - 1) {
					throw (new RuntimeException("Could not find " + fileName + " in any of the folders " + folderList));
				}
			}
		}
		
		return body;
	}
}
