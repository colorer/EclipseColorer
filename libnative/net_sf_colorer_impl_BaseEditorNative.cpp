
#include"net_sf_colorer_impl_BaseEditorNative.h"

#include"JBaseEditor.h"

jclass cStyledRegion = null;
jclass cTextRegion = null;
jclass cLineRegion  = null;

jmethodID idStyledRegionConstr;
jmethodID idTextRegionConstr;
jmethodID idLineRegionConstr;

int jbe_count = 0;

void createJNInfo(JNIEnv *env) {
  if (cStyledRegion == null){
    CLR_TRACE("BaseEditorNative", "createJNInfo: on null");
    cStyledRegion = (jclass)env->NewGlobalRef(env->FindClass("net/sf/colorer/handlers/StyledRegion"));
    cTextRegion = (jclass)env->NewGlobalRef(env->FindClass("net/sf/colorer/handlers/TextRegion"));
    cLineRegion = (jclass)env->NewGlobalRef(env->FindClass("net/sf/colorer/handlers/LineRegion"));

    if (cStyledRegion == null || cTextRegion == null || cLineRegion == null) {
      throw_exc(env, "FATAL: createJNInfo NewGlobalRef failed");
      return;
    }

    idStyledRegionConstr = env->GetMethodID(cStyledRegion, "<init>", "(ZZIII)V");
    idTextRegionConstr = env->GetMethodID(cTextRegion, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    idLineRegionConstr = env->GetMethodID(cLineRegion, "<init>", "(Lnet/sf/colorer/Region;Lnet/sf/colorer/handlers/RegionDefine;ZIILnet/sf/colorer/Scheme;)V");
    
    if (idStyledRegionConstr == null || idTextRegionConstr == null || idLineRegionConstr == null) {
      throw_exc(env, "FATAL: createJNInfo failed");
      return;
    }

  }
}

void dropJNInfo(JNIEnv *env) {
  env->DeleteGlobalRef(cStyledRegion);
  env->DeleteGlobalRef(cTextRegion);
  cStyledRegion = null;
  cTextRegion = null;
  env->DeleteGlobalRef(cLineRegion);
  cLineRegion = null;
}

jobject createRegionDefine(JNIEnv *env, const RegionDefine *rd)
{
  if (rd == null) return null;

  createJNInfo(env);
  
  if (rd->type == STYLED_REGION) {
    const StyledRegion *styled = StyledRegion::cast(rd);
    if (styled == null) {
      return null;
    }
    return env->NewObject(cStyledRegion, idStyledRegionConstr, styled->bfore, styled->bback, styled->fore, styled->back, styled->style);
  }
  if (rd->type == TEXT_REGION) {
    const TextRegion *texted = TextRegion::cast(rd);
    if (texted == null) {
      return null;
    }
    return env->NewObject(cTextRegion, idTextRegionConstr, env_NewString_or_null(texted->stext), env_NewString_or_null(texted->etext), env_NewString_or_null(texted->sback), env_NewString_or_null(texted->eback));
  }
  return null;
}


extern "C"{

JNIEXPORT jlong JNICALL Java_net_sf_colorer_impl_BaseEditorNative_init(JNIEnv *env, jobject obj, jobject pf, jobject ls){

  if (pf == null || ls == null){
    throw_exc(env, "Bad BaseEditor constructor parameters");
    return 0;
  };

  CLR_TRACE("BaseEditorNative", "init: %d", jbe_count+1);
  createJNInfo(env);

  JBaseEditor *jbe = null;
  
  try{
    jbe_count++;
    jclass cPF = env->FindClass("net/sf/colorer/ParserFactory");
    jfieldID idIptr = env->GetFieldID(cPF, "iptr", "J");
    JParserFactory *parserFactory = (JParserFactory *)env->GetLongField(pf, idIptr);
    JavaLineSource *lineSource = new JavaLineSource(env, ls);

    jbe = new JBaseEditor(parserFactory, lineSource);
    jbe->jpf = env->NewGlobalRef(pf);
    jbe->lineSource = lineSource;
  }catch(Exception &e){
    throw_exc(env, e.getMessage()->getChars()); return 0;
  }
  return (jlong)jbe;
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_finalize(JNIEnv *env, jobject obj, jlong iptr){
  JBaseEditor *be = JBaseEditor::get(env, iptr);

  if (be == null) return;

  int idx;
  for(idx = 0; idx < be->lrCache.size(); idx++)
    if (be->lrCache.elementAt(idx) != null) env->DeleteGlobalRef(be->lrCache.elementAt(idx));
  for(idx = 0; idx < be->jregionHandlers.size(); idx++)
    delete be->jregionHandlers.elementAt(idx);

  env->DeleteGlobalRef(be->jpf);
  delete be->lineSource;

  if (jbe_count == 1){
    dropJNInfo(env);
  };
  delete be;
  jbe_count--;
  CLR_TRACE("BaseEditorNative", "finalize: %d", jbe_count);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_setRegionCompact(JNIEnv *env, jobject obj, jlong iptr, jboolean compact)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->setRegionCompact(compact != 0);
}


JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_setFileType(JNIEnv *env, jobject obj, jlong iptr, jobject filetype)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  jclass cFileType = env->FindClass("net/sf/colorer/FileType");
  jfieldID id_iptr = env->GetFieldID(cFileType, "iptr", "J");
  FileType *ft = (FileType*)env->GetLongField(filetype, id_iptr);
  be->setFileType(ft);
}

JNIEXPORT jobject JNICALL Java_net_sf_colorer_impl_BaseEditorNative_chooseFileType(JNIEnv *env, jobject obj, jlong iptr, jstring filename)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->chooseFileType(&JString(env, filename));
  FileType *filetype = be->getFileType();
  jobject jft = be->pf->jhp->getFileType(env, filetype);
  return jft;
}

JNIEXPORT jobject JNICALL Java_net_sf_colorer_impl_BaseEditorNative_getFileType(JNIEnv *env, jobject obj, jlong iptr)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  FileType *filetype = be->getFileType();
  if (filetype == null) return null;
  jobject jft = be->pf->jhp->getFileType(env, filetype);
  return jft;
}

JNIEXPORT void JNICALL JNICALL Java_net_sf_colorer_impl_BaseEditorNative_setRegionMapper__JLjava_lang_String_2Ljava_lang_String_2
(JNIEnv *env, jobject obj, jlong iptr, jstring cls, jstring name)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  try{
    be->setRegionMapper(&JString(env, cls), &JString(env, name));
  }catch(Exception &e){
    throw_exc(env, e.getMessage()->getChars());
    return;
  }
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_setRegionMapper__JLnet_sf_colorer_handlers_RegionMapper_2
  (JNIEnv *env, jobject obj, jlong iptr, jobject jregionMapper)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);

  jclass jClass = env->FindClass("net/sf/colorer/handlers/RegionMapper");
  jfieldID id_iptr = env->GetFieldID(jClass, "iptr", "J");
  RegionMapper *regionMapper = (RegionMapper*)env->GetLongField(jregionMapper, id_iptr);
  if (regionMapper == null){
    CLR_ERROR("JBaseEditor", "Disposed RegionMapper was used");
    return;
  }

  try{
    be->setRegionMapper(regionMapper);
  }catch(Exception &e){
    throw_exc(env, e.getMessage()->getChars());
    return;
  }

}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_addRegionHandler
(JNIEnv *env, jobject obj, jlong iptr, jobject rh, jobject filter)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  const Region *filterRegion = null;

  if (filter != null){
    jmethodID gnID = env->GetMethodID(env->GetObjectClass(filter), "getName", "()Ljava/lang/String;");
    jstring filter_name = (jstring)env->CallObjectMethod(filter, gnID);
    filterRegion = be->pf->getHRCParser()->getRegion(&JString(env, filter_name));
  }
  CLR_TRACE("BaseEditorNative", "addRegionHandler0:%p", rh);
  JWrapRegionHandler *jwrh = new JWrapRegionHandler(env, be->pf->jhp, rh, filterRegion);
  be->jregionHandlers.addElement(jwrh);
  be->addRegionHandler(jwrh);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_removeRegionHandler(JNIEnv *env, jobject obj, jlong iptr, jobject rh){
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  JWrapRegionHandler *jwrh = null;
  CLR_TRACE("BaseEditorNative", "removeRegionHandler:%d", be->jregionHandlers.size());
  for(int idx = 0; idx < be->jregionHandlers.size(); idx++){
    JWrapRegionHandler *check = be->jregionHandlers.elementAt(idx);
    if (env->IsSameObject(check->regionHandler, rh)){
      be->removeRegionHandler(check);
      be->jregionHandlers.removeElement(check);
      delete check;
      return;
    };
  };
}


JNIEXPORT jobject JNICALL Java_net_sf_colorer_impl_BaseEditorNative_getBackground(JNIEnv *env, jobject obj, jlong iptr)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  return createRegionDefine(env, be->rd_def_Text);
}
JNIEXPORT jobject JNICALL Java_net_sf_colorer_impl_BaseEditorNative_getVertCross(JNIEnv *env, jobject obj, jlong iptr)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  return createRegionDefine(env, be->rd_def_VertCross);
}
JNIEXPORT jobject JNICALL Java_net_sf_colorer_impl_BaseEditorNative_getHorzCross(JNIEnv *env, jobject obj, jlong iptr)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  return createRegionDefine(env, be->rd_def_HorzCross);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_setBackParse
  (JNIEnv *env, jobject obj, jlong iptr, jint backParse)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->setBackParse(backParse);
}

JNIEXPORT jobjectArray JNICALL Java_net_sf_colorer_impl_BaseEditorNative_getLineRegions(JNIEnv *env, jobject obj, jlong iptr, jint lno)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);

  if (lno >= be->lrCache.size()){
    int newsize = be->lrCache.size()*2;
    if (newsize < lno+1) newsize = lno+1;
    be->lrCache.setSize(newsize);
  }

  CLR_TRACE("BaseEditorNative", "JNIEnv:%x", env);

  LineRegion *lregion = null;
  try{
    lregion = be->getLineRegions(lno);
  }catch(Exception &e){
    throw_exc(env, e.getMessage()->getChars());
    return null;
  }catch(...){
    throw_exc(env, "PANIC");
    return null;
  };

  jobjectArray cachedLR = be->lrCache.elementAt(lno);
  if (cachedLR != null && lno <= be->validLine){
    return cachedLR;
  }else{
    if (cachedLR != null) env->DeleteGlobalRef(cachedLR);
    for(int idx = be->validLine+1; idx < lno; idx++){
      if (be->lrCache.elementAt(idx) != null)
        env->DeleteGlobalRef(be->lrCache.elementAt(idx));
      be->lrCache.setElementAt(null, idx);
    };
  }

  int arrSize = 0;
  LineRegion *next = null;
  for(next = lregion; next != null; next = next->next) arrSize++;
  jobjectArray lrArray = env->NewObjectArray(arrSize, cLineRegion, null);

  int idx = 0;
  for(next = lregion; next != null; next = next->next, idx++){
    jobject sr = createRegionDefine(env, next->rdef);
    jobject region = null;
    if (next->region) region = be->pf->jhp->getRegion(env, next->region->getName());
    jobject scheme = null;
    if (next->scheme) scheme = be->pf->jhp->getScheme(env, next->scheme);
    jobject lr = env->NewObject(cLineRegion, idLineRegionConstr, region, sr, next->special, next->start, next->end, scheme);
    env->SetObjectArrayElement(lrArray, idx, lr);
    if (sr != null) env->DeleteLocalRef(sr);
    env->DeleteLocalRef(lr);
  };
  be->lrCache.setElementAt((jobjectArray)env->NewGlobalRef(lrArray), lno);
  be->validLine = lno;
  return lrArray;
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_validate
  (JNIEnv *env, jobject obj, jlong iptr, jint lno)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->validate(lno, true);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_idleJob
  (JNIEnv *env, jobject obj, jlong iptr, jint time)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->idleJob(time);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_modifyEvent(JNIEnv *env, jobject obj, jlong iptr, jint topLine)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->modifyEvent(topLine);
  if (be->validLine > topLine-1) be->validLine = topLine-1;
}


JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_modifyLineEvent(JNIEnv *env, jobject obj, jlong iptr, jint line)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->modifyLineEvent(line);
  if (be->validLine > line-1) be->validLine = line-1;
}


JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_visibleTextEvent(JNIEnv *env, jobject obj, jlong iptr, jint wStart, jint wSize)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->visibleTextEvent(wStart, wSize);
}


JNIEXPORT void JNICALL Java_net_sf_colorer_impl_BaseEditorNative_lineCountEvent(JNIEnv *env, jobject obj, jlong iptr, jint newLineCount)
{
  JBaseEditor *be = JBaseEditor::get(env, iptr);
  be->lineCountEvent(newLineCount);
}


};
