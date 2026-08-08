import zipfile, os
j=r'C:\Users\Black\.gradle\caches\modules-2\files-2.1\net.fabricmc.fabric-api\fabric-rendering-v1\16.2.10+0290ad933e\2485bb9579abaada1f556258b823dd9fbb1e3b05\fabric-rendering-v1-16.2.10+0290ad933e.jar'
zipfile.ZipFile(j).extract('net/fabricmc/fabric/api/client/rendering/v1/world/WorldRenderContext.class')
os.system('"C:\Program Files\Java\jdk-25\bin\javap.exe" net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext')
