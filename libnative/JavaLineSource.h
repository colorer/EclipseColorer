#ifndef _COLOERER_JAVALINESOURCE_H_
#define _COLOERER_JAVALINESOURCE_H_

#include<colorer/LineSource.h>

#include"JString.h"

class JavaLineSource : public LineSource{
public:
  JavaLineSource(JNIEnv *env, jobject jsource){
    this->env = env;
    this->jsource = env->NewGlobalRef(jsource);
    jclass jcLineSource = env->FindClass("net/sf/colorer/LineSource");
    getLineID = env->GetMethodID(jcLineSource, "getLine", "(I)Ljava/lang/String;");
    returnLine = null;
    CLR_TRACE("JavaLineSource", "JNIEnv:%x", env);
  };
  ~JavaLineSource(){
    delete returnLine;
    env->DeleteGlobalRef(jsource);
  };

  void startJob(int lno){};
  void endJob(int lno){
    delete returnLine;
    returnLine = null;
  };
  String *getLine(int lno){
    delete returnLine;
    returnLine = null;
    jstring line = (jstring)env->CallObjectMethod(jsource, getLineID, lno);
    jthrowable exc = env->ExceptionOccurred();
    if (exc){
      return null;
      //env->ExceptionDescribe();
      env->ExceptionClear();
      //throw Exception(StringBuffer("getLine request fault:")+SString(lno));
    };
    if (line == null) return null;

    returnLine = new JString(env, line);
    return returnLine;
  };
  JNIEnv *env;
private:
  jobject jsource;
  jmethodID getLineID;
  String *returnLine;
  int lastIndex;
};

#endif