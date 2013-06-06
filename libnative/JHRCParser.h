#ifndef _COLORER_JHRCPARSER_H_
#define _COLORER_JHRCPARSER_H_

#include<common/Hashtable.h>
#include<colorer/HRCParser.h>

class JHRCParser{
  Hashtable<jobject> regions;
  Hashtable<jobject> fileTypes;
  Hashtable<jobject> schemesCache;

public:
  HRCParser *hrcParser;
  jobject jHRCParser;

  jobject getRegion(JNIEnv *env, int index)
  {
    CLR_TRACE("NSC:JHRCParser:getRegion", "index:%d, hp=%d", index, hrcParser);
    const Region *nreg = hrcParser->getRegion(index);
    if (nreg == null)
    {
      return null;
    }
    CLR_TRACE("NSC:JHRCParser:getRegion", "region:%s", nreg->getName()->getChars());
    return getRegion(env, nreg->getName());
  }

  jobject getRegion(JNIEnv *env, const String *regname)
  {
    jobject reg = regions.get(regname);
    if (reg == null)
    {
      const Region *nreg = hrcParser->getRegion(regname);
      if (nreg == null) return null;

      jclass cRegion = env->FindClass("net/sf/colorer/Region");
      jmethodID idRegionConstr = env->GetMethodID(cRegion, "<init>", "(Ljava/lang/String;Ljava/lang/String;Lnet/sf/colorer/Region;IJ)V");

      reg = env->NewObject(cRegion, idRegionConstr,
                          env_NewString(nreg->getName()),
                          nreg->getDescription() ? env_NewString(nreg->getDescription()) : null,
                          nreg->getParent() ? getRegion(env, nreg->getParent()->getName()) : null,
                          nreg->getID(),
                          (jlong)nreg
                        );
      reg = env->NewGlobalRef(reg);
      regions.put(regname, reg);
    }
    return reg;
  }

  jobject getScheme(JNIEnv *env, const Scheme *scheme)
  {
    jobject jscheme = schemesCache.get(scheme->getName());
    if (jscheme == null)
    {
      jclass cScheme = env->FindClass("net/sf/colorer/Scheme");
      jmethodID idSchemeConstr = env->GetMethodID(cScheme, "<init>", "(Ljava/lang/String;Lnet/sf/colorer/FileType;)V");

      jscheme = env->NewObject(cScheme, idSchemeConstr,
                           env_NewString(scheme->getName()),
                           getFileType(env, scheme->getFileType())
                          );
      jscheme = env->NewGlobalRef(jscheme);
      schemesCache.put(scheme->getName(), jscheme);
    }
    return jscheme;
  }

  jobject enumerateFileTypes(JNIEnv *env, int idx)
  {
    FileType *filetype = hrcParser->enumerateFileTypes(idx);
    if (filetype == null) return null;
    return getFileType(env, filetype);
  }

  jobject getFileType(JNIEnv *env, FileType *filetype){
    jobject jtype = fileTypes.get(filetype->getName());
    if (jtype == null)
    {
      jclass cFileType = env->FindClass("net/sf/colorer/FileType");
      jmethodID idFileTypeConstr = env->GetMethodID(cFileType, "<init>", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
      jtype = env->NewObject(cFileType, idFileTypeConstr, (jlong)filetype,
                          env_NewString(filetype->getName()),
                          filetype->getGroup() ? env_NewString(filetype->getGroup()) : null,
                          filetype->getDescription() ? env_NewString(filetype->getDescription()) : null
                        );
      jtype = env->NewGlobalRef(jtype);
      fileTypes.put(filetype->getName(), jtype);
    }
    return jtype;
  }

  void finalize(JNIEnv *env){
      for(jobject region = regions.enumerate(); region != null; region = regions.next()) {
        env->DeleteGlobalRef(region);
      }
      for(jobject filetype = fileTypes.enumerate(); filetype != null; filetype = fileTypes.next()) {
        env->DeleteGlobalRef(filetype);
      }
      env->DeleteGlobalRef(jHRCParser);
      delete this;
  }

private:
  ~JHRCParser(){}

};

#endif