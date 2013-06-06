#ifndef _COLORER_JPARSERFACTORY_H_
#define _COLORER_JPARSERFACTORY_H_

#include<colorer/ParserFactory.h>

#include"JHRCParser.h"

class JParserFactory : public ParserFactory
{
public:
    JParserFactory(String *catalog) : ParserFactory(catalog)
    {
        jhp = null;
    }

    JHRCParser *jhp;
};

#endif