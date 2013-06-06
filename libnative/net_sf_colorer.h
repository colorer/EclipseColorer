
#include<jni.h>

#include<common/Common.h>
#include<common/Logging.h>
#include<colorer/editor/BaseEditor.h>

#include"JavaLineSource.h"
#include"JString.h"

void throw_exc(JNIEnv *env, const char *msg);

jobject createRegionDefine(JNIEnv *env, const RegionDefine *rd);
