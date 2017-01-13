REM File: ant-scp-copyToFlickerSFmirror.bat - scp Flicker to open2dprot.sf.net
REM Flicker mirror mirrorFlicker
REM P. Lemkin $Date$ 

echo "ant-scp-mirrorsBuildCSDToSF.bat"

pwd
date /T 

ant.bat -Dusername=%USERNAME% -Dpassword=%PASSWD% -file scp-copyToFlickerSFmirror.xml -logfile scp-copyToFlickerSFmirror.log copyFlicker

echo "Finished ant-scp-copyToFlickerSFmirror.bat"

date /T 
