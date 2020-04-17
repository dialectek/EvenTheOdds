// Record manager.

package com.dialectek.even_the_odds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RecordManager
{
   private int      Browse          = 0;
   private int      Save            = 1;
   private int      Select_type     = Save;
   private String   m_dataDirectory = "";
   private Context  m_context;
   private TextView m_operationView;
   private TextView m_titleView;
   public static String    Default_File_Name  = "";
   private String   Selected_File_Name = Default_File_Name;
   private EditText input_text;

   private String               m_dir         = "";
   private List<String>         m_subdirs     = null;
   private Listener             m_Listener    = null;

   private List<String>         m_showdirs    = null;
   private ArrayAdapter<String> m_listAdapter = null;
   private final int MAX_SHOW_DIRS            = 3;
   private int                  m_showIndex   = 0;
   Button m_backButton                        = null;
   Button m_upButton                          = null;
   Button m_downButton                        = null;

   // Callback interface for selected directory.
   public interface Listener
   {
      public void onChosenDir(String chosenDir);
   }

   public RecordManager(Context context, String record_select_type, Listener listener)
   {
      if (record_select_type.equals("Browse")) { Select_type = Browse; }
      else if (record_select_type.equals("Save")) { Select_type = Save; }
      else{ Select_type = Browse; }

      m_context       = context;
      m_dataDirectory = context.getFilesDir().getAbsolutePath();
      m_Listener      = listener;

      try
      {
         m_dataDirectory = new File(m_dataDirectory).getCanonicalPath();
      }
      catch (IOException ioe)
      {
      }
   }


   // Load directory chooser dialog for initial default directory.
   public void chooseFile_or_Dir()
   {
      // Initial directory is root directory
      if (m_dir.equals("")) { chooseFile_or_Dir(m_dataDirectory); }
      else{ chooseFile_or_Dir(m_dir); }
   }


   // Load directory chooser dialog for initial input 'dir' directory.
   public void chooseFile_or_Dir(String dir)
   {
      File dirFile = new File(dir);

      if (!dirFile.exists() || !dirFile.isDirectory())
      {
         dir = m_dataDirectory;
      }

      try
      {
         dir = new File(dir).getCanonicalPath();
      }
      catch (IOException ioe)
      {
         return;
      }

      m_dir     = dir;
      m_subdirs = getDirectories(dir);

      class Listener implements DialogInterface.OnClickListener
      {
         public void onClick(DialogInterface dialog, int item)
         {
            String m_dir_old = m_dir;
            String sel       = "" + ((AlertDialog)dialog).getListView().getAdapter().getItem(item);

            if (sel.charAt(sel.length() - 1) == '/') { sel = sel.substring(0, sel.length() - 1); }

            m_dir += "/" + sel;
            Selected_File_Name = Default_File_Name;

            if ((new File(m_dir).isFile()))
            {
               m_dir = m_dir_old;
               Selected_File_Name = sel;
            } else {
               m_showIndex = 0;
            }

            updateDirectory();
         }
      }

      AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, new Listener());

      if (Select_type == Browse)
      {
         dialogBuilder.setPositiveButton("Play", new OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                       if (m_Listener != null)
                       {
                          Selected_File_Name = input_text.getText() + "";
                          m_Listener.onChosenDir(m_dir + "/" + Selected_File_Name);
                       }
                    }
                 }
         );
         dialogBuilder.setNegativeButton("Delete", new OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                       Selected_File_Name = input_text.getText() + "";
                       String deleteFile = m_dir + "/" + Selected_File_Name;
                       if (!deleteDirFile(deleteFile))
                       {
                          Toast.makeText(m_context, "Cannot delete '"
                                  + deleteFile, Toast.LENGTH_LONG).show();
                       }
                       if (m_Listener != null)
                       {
                          m_Listener.onChosenDir(m_dir + "/" + Selected_File_Name);
                       }
                    }
                 }
         );
         dialogBuilder.setNeutralButton("Cancel", null);
      } else {
         // Save.
         dialogBuilder.setPositiveButton("OK", new OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                       Selected_File_Name = input_text.getText() + "";
                       String newFile = m_dir + "/" + Selected_File_Name;
                       if (Selected_File_Name.isEmpty() || !createFile(newFile))
                       {
                          Toast.makeText(m_context, "Cannot create '"
                                  + newFile, Toast.LENGTH_LONG).show();
                       }
                       if (m_Listener != null)
                       {
                          m_Listener.onChosenDir(m_dir + "/" + Selected_File_Name);
                       }
                    }
                 }
         ).setNegativeButton("Cancel", null);
      }

      final AlertDialog dirsDialog = dialogBuilder.create();

      dirsDialog.show();
   }


   private boolean createSubDir(String newDir)
   {
      File newDirFile = new File(newDir);

      if (!newDirFile.exists()) { return(newDirFile.mkdir()); }
      else{ return(false); }
   }

   private boolean createFile(String newFile)
   {
      File newDirFile = new File(newFile);

      try
      {
         return newDirFile.createNewFile();
      } catch (Exception e) {}
      return(false);
   }

   private boolean deleteDirFile(String deleteFile)
   {
      if (m_dataDirectory.equals(deleteFile))
      {
         return false;
      }

      File dirFile = new File(deleteFile);

      if (!dirFile.exists())
      {
         return(false);
      }

      if (dirFile.isDirectory())
      {
         if (dirFile.listFiles().length > 0)
         {
            return false;
         }
      }

      return dirFile.delete();
   }

   private List<String> getDirectories(String dir)
   {
      List<String> dirs = new ArrayList<String>();
      try
      {
         File dirFile = new File(dir);

         if (!dirFile.exists() || !dirFile.isDirectory())
         {
            return(dirs);
         }

         for (File file : dirFile.listFiles())
         {
            if (file.isDirectory())
            {
               // Add "/" to directory names to identify them in the list.
               dirs.add(file.getName() + "/");
            }
            else
            {
               dirs.add(file.getName());
            }
         }
      }
      catch (Exception e) {}

      Collections.sort(dirs, new Comparator<String>()
                       {
                          public int compare(String o1, String o2)
                          {
                             return(o1.compareTo(o2));
                          }
                       }
                       );
      return(dirs);
   }


   /// Dialog definition.
   private AlertDialog.Builder createDirectoryChooserDialog(String title,
                                                            DialogInterface.OnClickListener onClickListener)
   {
      AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);

      m_operationView = new TextView(m_context);
      m_operationView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

      if (Select_type == Browse) { m_operationView.setText("Browse:"); }
      if (Select_type == Save) { m_operationView.setText("Save:"); }

      m_operationView.setGravity(Gravity.CENTER_VERTICAL);
      m_operationView.setBackgroundColor(-12303292);
      m_operationView.setTextColor(m_context.getResources().getColor(android.R.color.white));

      LinearLayout operationLayout = new LinearLayout(m_context);
      operationLayout.setOrientation(LinearLayout.VERTICAL);
      operationLayout.addView(m_operationView);

      if (Select_type == Save)
      {
         // Create New Folder button.
         Button newDirButton = new Button(m_context);
         newDirButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
         newDirButton.setText("New Folder");
         newDirButton.setOnClickListener(new View.OnClickListener()
                                         {
                                            @Override
                                            public void onClick(View v)
                                            {
                                               final EditText input = new EditText(m_context);

                                               // Show new folder name input dialog
                                               new AlertDialog.Builder(m_context).
                                                  setTitle("New Folder Name").
                                                  setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener()
                                                                                   {
                                                                                      public void onClick(DialogInterface dialog, int whichButton)
                                                                                      {
                                                                                         Editable newDir = input.getText();
                                                                                         String newDirName = newDir.toString();

                                                                                         // Create new directory.
                                                                                         if (createSubDir(m_dir + "/" + newDirName))
                                                                                         {
                                                                                            // Navigate into the new directory.
                                                                                            m_dir += "/" + newDirName;
                                                                                            updateDirectory();
                                                                                         }
                                                                                         else
                                                                                         {
                                                                                            Toast.makeText(m_context, "Cannot create '"
                                                                                                           + newDirName + "' folder", Toast.LENGTH_LONG).show();
                                                                                         }
                                                                                      }
                                                                                   }
                                                                                   ).setNegativeButton("Cancel", null).show();
                                            }
                                         }
                                         );
         operationLayout.addView(newDirButton);
      }

      // Create navigation buttons.
      LinearLayout navigationLayout = new LinearLayout(m_context);
      navigationLayout.setOrientation(LinearLayout.HORIZONTAL);
      m_backButton = new Button(m_context);
      m_backButton.setEnabled(false);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
      m_backButton.setLayoutParams(params);
      m_backButton.setText("Back");
      m_backButton.setOnClickListener(new View.OnClickListener()
                                      {
                                         @Override
                                         public void onClick(View v)
                                         {
                                            if (!m_dir.equals(m_dataDirectory))
                                            {
                                               m_dir = m_dir.substring(0, m_dir.lastIndexOf("/"));
                                               m_showIndex = 0;
                                               updateDirectory();
                                            }
                                         }
                                      }
      );
      navigationLayout.addView(m_backButton);
      m_upButton = new Button(m_context);
      m_upButton.setEnabled(false);
      m_upButton.setLayoutParams(params);
      m_upButton.setText("Up");
      m_upButton.setOnClickListener(new View.OnClickListener()
                                    {
                                       @Override
                                       public void onClick(View v)
                                       {
                                          if (m_showIndex > 0)
                                          {
                                             m_showIndex--;
                                             updateDirectory();
                                          }
                                       }
                                    }
      );
      navigationLayout.addView(m_upButton);
      m_downButton = new Button(m_context);
      m_downButton.setEnabled(false);
      m_downButton.setLayoutParams(params);
      m_downButton.setText("Down");
      m_downButton.setOnClickListener(new View.OnClickListener()
                                  {
                                     @Override
                                     public void onClick(View v)
                                     {
                                        if ((MAX_SHOW_DIRS * (m_showIndex + 1)) < m_subdirs.size())
                                        {
                                           m_showIndex++;
                                           updateDirectory();
                                        }
                                     }
                                  }
      );
      navigationLayout.addView(m_downButton);
      operationLayout.addView(navigationLayout);

      // Create view with folder path and entry text box.
      LinearLayout titleLayout = new LinearLayout(m_context);
      titleLayout.setOrientation(LinearLayout.VERTICAL);
      m_titleView = new TextView(m_context);
      m_titleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
      m_titleView.setBackgroundColor(-12303292);           // dark gray -12303292
      m_titleView.setTextColor(m_context.getResources().getColor(android.R.color.white));
      m_titleView.setGravity(Gravity.CENTER_VERTICAL);
      m_titleView.setText(title);
      titleLayout.addView(m_titleView);

      // Add inpuut text.
      input_text = new EditText(m_context);
      input_text.setText(Default_File_Name);
      titleLayout.addView(input_text);

      // Set views and finish dialog builder.
      dialogBuilder.setView(titleLayout);
      dialogBuilder.setCustomTitle(operationLayout);
      m_showIndex = 0;
      m_showdirs = new ArrayList<String>();
      m_listAdapter = createListAdapter(m_showdirs);
      updateDirectory();
      dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
      dialogBuilder.setCancelable(false);
      return(dialogBuilder);
   }


   private void updateDirectory()
   {
      m_subdirs.clear();
      m_subdirs.addAll(getDirectories(m_dir));
      m_titleView.setText(m_dir);
      m_showdirs.clear();
      int i = MAX_SHOW_DIRS * m_showIndex;
      for (int j = 0; j < MAX_SHOW_DIRS && i < m_subdirs.size(); j++, i++)
      {
         m_showdirs.add(m_subdirs.get(i));
      }
      m_listAdapter.notifyDataSetChanged();
      input_text.setText(Selected_File_Name);
      if (m_dir.equals(m_dataDirectory))
      {
         m_backButton.setEnabled(false);
      } else {
         m_backButton.setEnabled(true);
      }
      if (m_showIndex > 0)
      {
         m_upButton.setEnabled(true);
      } else {
         m_upButton.setEnabled(false);
      }
      if ((MAX_SHOW_DIRS * (m_showIndex + 1)) < m_subdirs.size())
      {
         m_downButton.setEnabled(true);
      } else {
         m_downButton.setEnabled(false);
      }
      if (m_showIndex > 0)
      {
         m_upButton.setEnabled(true);
      } else {
         m_upButton.setEnabled(false);
      }
   }


   private ArrayAdapter<String> createListAdapter(List<String> items)
   {
      return(new ArrayAdapter<String>(m_context, android.R.layout.select_dialog_item, android.R.id.text1, items)
             {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                   View v = super.getView(position, convertView, parent);

                   if (v instanceof TextView)
                   {
                      TextView tv = (TextView)v;
                      tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                      tv.setEllipsize(null);
                   }
                   return v;
                }
             }
             );
   }
}
