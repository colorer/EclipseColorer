
#include"net_sf_colorer_handlers_RegionMapper.h"


JNIEXPORT jobject JNICALL Java_net_sf_colorer_handlers_RegionMapper_getRegionDefine__JLnet_sf_colorer_Region_2
  (JNIEnv *env, jobject obj, jlong iptr, jobject jregion)
{
    RegionMapper *rm = (RegionMapper*)iptr;

    jclass jClass = env->FindClass("net/sf/colorer/Region");
    jfieldID id_iptr = env->GetFieldID(jClass, "iptr", "J");
    Region *region = (Region*)env->GetLongField(jregion, id_iptr);

    if (region == null){
      CLR_WARN("JRegionMapper", "Region is empty");
      return null;
    }

    const RegionDefine *rdef = rm->getRegionDefine(region);
    if (rdef == null){
      CLR_WARN("JRegionMapper", "Region define is empty");
      return null;
    }

    return createRegionDefine(env, rdef);
}

JNIEXPORT jobject JNICALL Java_net_sf_colorer_handlers_RegionMapper_getRegionDefine__JLjava_lang_String_2
  (JNIEnv *env, jobject obj, jlong iptr, jstring name)
{
    RegionMapper *rm = (RegionMapper*)iptr;

    if (rm == null){
      CLR_WARN("JRegionMapper", "RM is null!!");
      return null;
    }

    const RegionDefine *rdef = rm->getRegionDefine(JString(env, name));
    if (rdef == null){
      CLR_WARN("JRegionMapper", "Region define is empty");
      return null;
    }

    return createRegionDefine(env, rdef);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_handlers_RegionMapper_finalize
  (JNIEnv *env, jobject obj, jlong iptr)
{
    RegionMapper *rm = (RegionMapper*)iptr;
    delete rm;
}

