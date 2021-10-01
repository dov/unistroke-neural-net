#!/usr/bin/python

# Use adb to get all the glyph files collected on a device

import subprocess,os

outdir = '/tmp/dov-uni-stroke'
if not os.path.exists(outdir):
    os.mkdir(outdir)

out = subprocess.run(['adb','shell','run-as','com.dovgro.unistroke','ls','-1'],
                    capture_output=True).stdout.decode('utf-8')
dbgfiles = [f for f in out.split('\n') if f.endswith('json')]
for f in dbgfiles:
    print(f)
    out = subprocess.run(['adb','shell','run-as','com.dovgro.unistroke','cat',f],
                        capture_output=True).stdout.decode('utf-8')
    with open(os.path.join(outdir,f),'w') as fh:
        fh.write(out)



