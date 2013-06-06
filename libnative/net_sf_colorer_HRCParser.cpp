
#include"net_sf_colorer_HRCParser.h"
#include"JHRCParser.h"

extern "C" {

JNIEXPORT jobject JNICALL Java_net_sf_colorer_HRCParser_getRegion(JNIEnv *env, jobject obj, jlong iptr, jstring qname) {
    JHRCParser *jhp = (JHRCParser*)iptr;
    CLR_TRACE("NSC:getRegion", "jhp:%d", jhp);
    return jhp->getRegion(env, &JString(env, qname));
}

JNIEXPORT jobject JNICALL Java_net_sf_colorer_HRCParser_getRegionByIndex(JNIEnv *env, jobject obj, jlong iptr, jint index) {
    JHRCParser *jhp = (JHRCParser*)iptr;
    CLR_TRACE("NSC:getRegionByIndex", "jhp:%d", jhp);
    return jhp->getRegion(env, index);
}

JNIEXPORT void JNICALL Java_net_sf_colorer_HRCParser_finalize(JNIEnv *env, jobject obj, jlong iptr) {
    JHRCParser *hp = (JHRCParser*)iptr;
    hp->finalize(env);
}

JNIEXPORT jobject JNICALL Java_net_sf_colorer_HRCParser_enumerateFileTypes
    (JNIEnv *env, jobject obj, jlong iptr, jint idx) {
    JHRCParser *hp = (JHRCParser*)iptr;
    return hp->enumerateFileTypes(env, idx);
}

}