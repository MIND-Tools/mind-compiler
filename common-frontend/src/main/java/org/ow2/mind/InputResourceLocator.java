
package org.ow2.mind;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

public interface InputResourceLocator {

  String ITF_NAME = "input-resource-locator";

  /**
   * Returns the URLs that are used as root directory to find input resources.
   * 
   * @param context additional parameters.
   * @return the URLs that are used as root directory to find input resources.
   */
  URL[] getInputResourcesRoot(Map<Object, Object> context);

  /**
   * Locate the given input resource.
   * 
   * @param resource a resource.
   * @param context additional parameters.
   * @return the {@link URL} of the resource file or <code>null</code> if no
   *         source file can be found for the given resource.
   */
  URL findResource(InputResource resource, Map<Object, Object> context);

  /**
   * Returns <code>true</code> if the given timestamp is greater that the
   * timestamps of the given input resources. If one of the given
   * {@link InputResource} can't be located, this method return
   * <code>false</code>.
   * 
   * @param timestamp a timestamp to check.
   * @param inputs a collection of input resources.
   * @return <code>true</code> if the given timestamp is greater that the
   *         timestamps of the given input resources.
   */
  boolean isUpToDate(long timestamp, Collection<InputResource> inputs,
      Map<Object, Object> context);

  /**
   * Returns <code>true</code> if the timestamp of the given file is greater
   * that the timestamps of the given input resources. If one of the given
   * {@link InputResource} can't be located, this method return
   * <code>false</code>.
   * 
   * @param file a file to check.
   * @param inputs a collection of input resources.
   * @return <code>true</code> if the given timestamp is greater that the
   *         timestamps of the given input resources.
   * @see File#lastModified()
   * @see #isUpToDate(long, Collection)
   */
  boolean isUpToDate(File file, Collection<InputResource> inputs,
      Map<Object, Object> context);

  /**
   * Returns <code>true</code> if the timestamp of the given URL is greater that
   * the timestamps of the given input resources. If one of the given
   * {@link InputResource} can't be located, this method return
   * <code>false</code>.
   * 
   * @param file a file to check.
   * @param inputs a collection of input resources.
   * @return <code>true</code> if the given timestamp is greater that the
   *         timestamps of the given input resources.
   * @throws MalformedURLException
   * @see InputResourcesHelper#getTimestamp(URL)
   * @see #isUpToDate(long, Collection)
   */
  boolean isUpToDate(URL url, Collection<InputResource> inputs,
      Map<Object, Object> context) throws MalformedURLException;
}
