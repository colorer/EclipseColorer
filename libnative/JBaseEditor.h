#ifndef _COLORER_JBASEEDITOR_H_
#define _COLORER_JBASEEDITOR_H_

#include"JavaLineSource.h"
#include"JParserFactory.h"
#include"JWrapRegionHandler.h"

/** Java wrapper for BaseEditor class.
    Stores global reference to it's ParserFactory object.
    LineSource reference is used to update JNIEnv pointer while functions calls.
    @ingroup colorer_java
*/
class JBaseEditor : public BaseEditor{
public:
  JBaseEditor(JParserFactory *pf, JavaLineSource *lineSource):BaseEditor(pf, lineSource){
    this->pf = pf;
    validLine = -1;
  };
  static JBaseEditor *get(JNIEnv *env, jlong iptr){
    JBaseEditor *be = (JBaseEditor*)(iptr);
    be->lineSource->env = env;
    for(int idx = 0; idx < be->jregionHandlers.size(); idx++)
      be->jregionHandlers.elementAt(idx)->env = env;
    return be;
  };

  jobject jpf;
  JParserFactory *pf;
  JavaLineSource *lineSource;
  // Cache of created color regions.
  Vector<jobjectArray> lrCache;
  // Currently installed handlers
  Vector<JWrapRegionHandler*> jregionHandlers;
  // last valid line. used to invalidate cache state.
  int validLine;
};


#endif