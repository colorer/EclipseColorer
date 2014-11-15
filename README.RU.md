EclipseColorer
========================
EclipseColorer - плагин подсветки синтаксиса для Eclipse

Состав
------------------------

Плагин состоит из
  * src - java библиотеки
  * libnative - библиотеки на C++ с основными функциями разбора текста
  * schemes - библиотеки схем

Поскольку libnative специфична для каждой платформы, на которой может быть запущен плагин, его сборка выполняется 
отдельно под каждой из платформ. Готовые библиотеки складываются в папку distr/os и используются при сборке плагина.

Сборка библиотеки схем
------------------------

Для сборки библиотеки необходимо

  * git
  * ant 1.8 или выше
  * java development kit 6 (jdk) или выше
  * perl
  * eclipse

Скачиваем последние исходники с github

    git clone https://github.com/colorer/EclipseColorer.git --recursive

или обновляем репозиторий

    git pull
    git submodule update --recursive

Указываем путь до Eclipse в файле build.properties в переменной eclipse.dir
Запускаем сборку

    ant

При ошибках сборки схем, требуется ознакомится с README.md из папки schemes.

Для сборки libnative необходимо
 
  * выполнить 'ant colorer.jar'
  * в папке libnative вызвать команду make для соответствующей платформы

Ссылки
------------------------

* Сайт проекта: [http://colorer.sourceforge.net/](http://colorer.sourceforge.net/)
