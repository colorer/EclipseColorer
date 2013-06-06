#ifndef _COLORER_JSTRING_H_
#define _COLORER_JSTRING_H_

#include<unicode/String.h>

#define env_NewString(s) env->NewString((jchar*)s->getWChars(), s->length())

#define env_NewString_or_null(string) ((string == null) ? null : env_NewString(string))

class JString : public String{
public:
  JString(JNIEnv *env, jstring jdstring){
    jdstring = (jstring)env->NewGlobalRef(jdstring);
    jboolean copied;
    
    const jchar *chars = env->GetStringChars(jdstring, &copied);
    this->len = env->GetStringLength(jdstring);
    
    this->chars = new jchar[len];
    memcpy(this->chars, chars, len*sizeof(jchar));
    
    env->ReleaseStringChars(jdstring, chars);
    env->DeleteGlobalRef(jdstring);
  };
  ~JString(){
    delete[] this->chars;
  };

  int length() const{
    return len;
  };
  wchar operator[](int idx) const{
    if (idx < 0 || idx >= len) throw OutOfBoundException(StringBuffer("JString: ")+SString(idx));
    return this->chars[idx];
  };
private:  
  jchar *chars;
  int len;
};

#endif