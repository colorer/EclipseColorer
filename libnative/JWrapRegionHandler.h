#ifndef _COLORER_JWRAPREGIONHANDLER_H_
#define _COLORER_JWRAPREGIONHANDLER_H_

#include<jni.h>
#include<colorer/RegionHandler.h>

#include"JHRCParser.h"

/** Java Wrapper for RegionHandler interface
*/
class JWrapRegionHandler : public RegionHandler
{
private:
  jmethodID spID, epID, clID, arID, esID, lsID;
  const Region *filter;
  JHRCParser *hrcParser;

public:
  JNIEnv *env;
  /// reference to the Java interface
  jobject regionHandler;

  JWrapRegionHandler(JNIEnv *env, JHRCParser *hp, jobject rh, const Region *filter){
    this->env = env;
    this->filter = filter;
    hrcParser = hp;
    regionHandler = env->NewGlobalRef(rh);
    jclass jcRegionHandler = env->GetObjectClass(regionHandler);
    spID = env->GetMethodID(jcRegionHandler, "startParsing", "(I)V");
    epID = env->GetMethodID(jcRegionHandler, "endParsing", "(I)V");
    clID = env->GetMethodID(jcRegionHandler, "clearLine", "(ILjava/lang/String;)V");
    arID = env->GetMethodID(jcRegionHandler, "addRegion", "(ILjava/lang/String;IILnet/sf/colorer/Region;)V");
    esID = env->GetMethodID(jcRegionHandler, "enterScheme", "(ILjava/lang/String;IILnet/sf/colorer/Region;Ljava/lang/String;)V");
    lsID = env->GetMethodID(jcRegionHandler, "leaveScheme", "(ILjava/lang/String;IILnet/sf/colorer/Region;Ljava/lang/String;)V");
  }

  ~JWrapRegionHandler(){
    env->DeleteGlobalRef(regionHandler);
  }

  void startParsing(int lno){
    env->CallVoidMethod(regionHandler, spID, lno);
  }
  void endParsing(int lno){
    env->CallVoidMethod(regionHandler, epID, lno);
  }
  void clearLine(int lno, String *line){
    // make original Java string passing!!!
    env->CallVoidMethod(regionHandler, clID, lno, env_NewString(line));
  }
  void addRegion(int lno, String *line, int sx, int ex, const Region *region){
    if (filter != null && !region->hasParent(filter)) return;
    jobject jr = hrcParser->getRegion(env, region->getName());
    env->CallVoidMethod(regionHandler, arID, lno, env_NewString(line), sx, ex, jr);
  }
  void enterScheme(int lno, String *line, int sx, int ex, const Region *region, const Scheme *scheme){
    jobject jr = null;
    if (region != null) jr = hrcParser->getRegion(env, region->getName());
    env->CallVoidMethod(regionHandler, esID, lno, env_NewString(line), sx, ex, jr, env_NewString(scheme->getName()));
  }
  void leaveScheme(int lno, String *line, int sx, int ex, const Region *region, const Scheme *scheme){
    jobject jr = null;
    if (region != null) jr = hrcParser->getRegion(env, region->getName());
    env->CallVoidMethod(regionHandler, lsID, lno, env_NewString(line), sx, ex, jr, env_NewString(scheme->getName()));
  }
};

#endif