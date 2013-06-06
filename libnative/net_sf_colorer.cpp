
#include"net_sf_colorer.h"

#include"net_sf_colorer_HRCParser.cpp"
#include"net_sf_colorer_FileType.cpp"
#include"net_sf_colorer_ParserFactory.cpp"
#include"net_sf_colorer_impl_BaseEditorNative.cpp"
#include"net_sf_colorer_handlers_RegionMapper.cpp"

void  throw_exc(JNIEnv *env, const char *msg){
  jclass cException = env->FindClass("java/lang/Exception");
  env->ThrowNew(cException, msg);
}
