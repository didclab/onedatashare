package org.onedatashare.server.model.core;

import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class Stat {
  /**
   * The id of the resource (Used in Google Drive SDK for now)
   */
  public String id;
  /**
   * The name of the resource.
   */
  public String name;
  /**
   * The size of the resource in bytes.
   */
  public long size;
  /**
   * The modification time of the resource in Unix time.
   */
  public long time;
  /**
   * Whether or not the resource is a directory.
   */
  public boolean dir;
  /**
   * Whether or not the resource is a file.
   */
  public boolean file;
  /**
   * If the resource is a link, the link target.
   */
  public String link;
  /**
   * An implementation-specific permissions string.
   */
  public String perm;
  /**
   * An array of subresources, if known.
   */
  public Stat[] files;

  public List<Stat> filesList;

  private transient long total_size = -1;
  private transient long total_num = 0;

  /**
   * Create a new {@code Stat} with no name.
   */
  public Stat() {
    this(null);
  }

  /**
   * Create a new {@code Stat} with the given name.
   */
  public Stat(String name) {
    this.name = name;
  }

  /**
   * Get the total size of the tree.
   */
  public long size() {
    if (total_size >= 0) {
      return total_size;
    }
    if (dir) {
      long s = size;
      if (files != null) for (Stat f : files)
        s += f.dir ? 0 : f.size();
      return total_size = s;
    }
    return total_size = size;
  }

  /**
   * Copy the data from the passed file tree into this one.
   */
  public Stat copy(Stat ft) {
    name = ft.name;
    size = ft.size;
    time = ft.time;
    dir = ft.dir;
    file = ft.file;
    link = ft.link;
    perm = ft.perm;
    return this;
  }

  /**
   * Get the total number of items under this tree.
   */
  public long count() {
    if (total_num > 0) {
      return total_num;
    }
    if (dir) {
      long n = 1;
      if (files != null) for (Stat f : files)
        n += f.count();
      return total_num = n;
    }
    return total_num = 1;
  }

  /**
   * Return a path up to the parent.
   */
  public String path() {
    return name;
  }

  /**
   * Set the files underneath this tree and reset cached values.
   */
  public Stat setFiles(Collection<Stat> fs) {
    return setFiles(fs.toArray(new Stat[fs.size()]));
  }

  public void setFilesList(List<Stat> fs){
    this.filesList = fs;
  }

  public String toString(){
    return name;
  }

  public List<Stat> getFilesList(){
    return this.filesList;
  }

  /**
   * Set the files underneath this tree and reset cached values.
   */
  public Stat setFileNames(Collection<String> fs) {
    return setFiles(fs.toArray(new String[fs.size()]));
  }

  /**
   * Set the files underneath this tree and reset cached values.
   */
  public Stat setFiles(Stat[] fs) {
    files = fs;
    total_size = -1;
    total_num = 0;

    return this;
  }

  /**
   * Set the files underneath this tree given only their names.
   */
  public Stat setFiles(String[] names) {
    if (names == null) {
      files = null;
    } else {
      Stat[] stats = new Stat[names.length];
      for (int i = 0; i < stats.length; i++)
        stats[i] = new Stat(names[i]);
      files = stats;
      total_size = -1;
      total_num = 0;
    }

    return this;
  }
}
