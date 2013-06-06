
#include"JBaseEditor.h"

extern "C" {

JNIEXPORT jstring JNICALL Java_net_sf_colorer_FileType_enumerateParameters
  (JNIEnv *env, jobject obj, jlong iptr, jint index)
{
  FileType *type = (FileType*)iptr;

  const String *param_name = type->enumerateParameters(index);
  if (param_name == null){
    return null;
  }
  return env_NewString(param_name);
}

JNIEXPORT jstring JNICALL Java_net_sf_colorer_FileType_getParameterDescription
  (JNIEnv *env, jobject obj, jlong iptr, jstring pname)
{
  FileType *type = (FileType*)iptr;

  const String *param_descr = type->getParameterDescription(JString(env, pname));

  if (param_descr == null){
    return null;
  }
  return env_NewString(param_descr);
}

JNIEXPORT jstring JNICALL Java_net_sf_colorer_FileType_getParamValue
  (JNIEnv *env, jobject obj, jlong iptr, jstring pname)
{
  FileType *type = (FileType*)iptr;

  const String *param_value = type->getParamValue(JString(env, pname));

  if (param_value == null){
    return null;
  }
  return env_NewString(param_value);
}

JNIEXPORT jstring JNICALL Java_net_sf_colorer_FileType_getParamDefaultValue
  (JNIEnv *env, jobject obj, jlong iptr, jstring pname)
{
  FileType *type = (FileType*)iptr;

  const String *param_value = type->getParamDefaultValue(JString(env, pname));

  if (param_value == null){
    return null;
  }
  return env_NewString(param_value);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_FileType_setParamValue
  (JNIEnv *env, jobject obj, jlong iptr, jstring pname, jstring pval)
{
  FileType *type = (FileType*)iptr;

  type->setParamValue(JString(env, pname), &JString(env, pval));
}

JNIEXPORT jobject JNICALL Java_net_sf_colorer_FileType_getBaseScheme
  (JNIEnv *env, jobject obj, jlong iptr)
{
  FileType *type = (FileType*)iptr;

  type->getBaseScheme();
  return null;
}


}
