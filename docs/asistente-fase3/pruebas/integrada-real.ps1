param([string]$BaseUrl='http://localhost:8080')
$ErrorActionPreference='Stop'
$session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
$base=$BaseUrl.TrimEnd('/')
$calls=New-Object 'System.Collections.Generic.HashSet[string]'
$script:step=0
function Assert-Test([bool]$condition,[string]$label){if(-not $condition){throw "NO APROBADO: $label"}}
function Chat([string]$message){
 $body=ConvertTo-Json @{message=$message} -Compress
 $script:step++
 Write-Host ('Paso real '+$script:step)
 try {$r=Invoke-RestMethod -Uri ($base+'/api/v1/public/assistant/chat') -Method POST -WebSession $session -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 180}
 catch {
   $detail=$_.Exception.Message
   try {$problem=$_.ErrorDetails.Message | ConvertFrom-Json; if($problem.error){$detail=$problem.error}; if($problem.diagnostico){$detail+=' ['+($problem.diagnostico.rutas -join ',')+']'}}catch{}
   throw ('Paso '+$script:step+': '+$detail)
 }
 foreach($c in @($r.herramientas)){
   Assert-Test ($calls.Add($c.id)) 'evidencia nueva por llamada'
   Write-Host ("{0} | {1} | {2} | exito={3}" -f $c.id,$c.herramienta,$c.tipo,$c.exito)
 }
 return $r
}
function No-Effects($r){Assert-Test (@($r.herramientas | Where-Object {$_.tipo -eq 'efecto'}).Count -eq 0) 'cero efectos antes de conformidad / durante consulta'}
function Has-Read($r,[string]$name){Assert-Test (@($r.herramientas | Where-Object {$_.herramienta -eq $name -and $_.tipo -eq 'lectura' -and $_.exito}).Count -gt 0) ('lectura real '+$name)}
try {
 try {$null=Invoke-WebRequest -UseBasicParsing -Uri ($base+'/asistente/index.html') -TimeoutSec 10}
 catch {throw 'PENDIENTE: no se pudo acceder a la pagina de Spring en BaseUrl; iniciar el backend con su entorno configurado.'}
 $r=Chat 'Como se reprograma un turno confirmado?'
 Assert-Test ($r.retrieved.id -eq 'INST-06') 'RAG institucional de reprogramacion'
 No-Effects $r
 $r=Chat 'Quiero consultar los turnos disponibles para solicitar uno.'
 Has-Read $r 'disponibilidad'; No-Effects $r
 $r=Chat 'Volve a consultar la agenda actual antes de elegir.'
 Has-Read $r 'disponibilidad'; No-Effects $r
 Assert-Test (@($r.horarios).Count -gt 0) 'existe al menos un cupo real valido para probar registro'
 $atTen=@($r.horarios | Where-Object {$local=[DateTimeOffset]::Parse($_.inicio).ToOffset([TimeSpan]::FromHours(-3)); $local.Hour -eq 10 -and $local.Minute -eq 0}); $slot=if($atTen.Count -gt 0){$atTen[0]}else{@($r.horarios)[0]}
 $when=[DateTimeOffset]::Parse($slot.inicio)
 # Backend sends BA offset. Assert the whole offered interval against the normative recurring window.
 foreach($s in @($r.horarios)){
   $a=[DateTimeOffset]::Parse($s.inicio).ToOffset([TimeSpan]::FromHours(-3))
   $b=[DateTimeOffset]::Parse($s.fin).ToOffset([TimeSpan]::FromHours(-3))
   Assert-Test ($a.DayOfWeek -ne [DayOfWeek]::Saturday -and $a.DayOfWeek -ne [DayOfWeek]::Sunday -and $a.Date -eq $b.Date -and $a.Hour -ge 9 -and $b.TimeOfDay -le [TimeSpan]::FromHours(18)) 'cupos compatibles con INST-01'
 }
 $choice='Me sirve el '+$when.ToString('yyyy-MM-dd')+' a las '+$(if($when.Minute -eq 0){$when.Hour.ToString()}else{$when.ToString('HH:mm')})+'. Todavia no envies ninguna solicitud.'
 $r=Chat $choice; No-Effects $r
 Assert-Test ($r.conversation.details.horarioId -eq $slot.id) 'coincidencia exacta del inicio de franja'
 $r=Chat 'Soy Persona Demo, correo persona.demo@example.com, motivo control general. Prepara el resumen sin enviar.'
 No-Effects $r
 Assert-Test ($r.booking.stage -eq 'resumen') 'resumen preparado'
 $r=Chat 'Cambia mi correo a persona.corregida@example.com y confirmo.'
 No-Effects $r
 Assert-Test ($r.booking.stage -eq 'resumen' -and $r.booking.details.correo -eq 'persona.corregida@example.com') 'correccion invalida conformidad y presenta resumen nuevo'
 $r=Chat 'Ahora si, confirmo el resumen que acabas de mostrar y autorizo enviar esa solicitud. Entiendo que queda pendiente de confirmacion por recepcion.'
 Assert-Test (@($r.herramientas | Where-Object {$_.tipo -eq 'efecto'}).Count -eq 1) 'un solo efecto tras conformidad'
 Assert-Test ($r.booking.stage -eq 'registrada' -and $r.booking.estado -eq 'pendiente') 'registro real pendiente'
 Write-Host ('Solicitud ficticia para comprobar en pgAdmin: '+$r.booking.solicitudId)
 $r=Chat 'Consulta el estado actual y que correo y motivo quedaron realmente guardados.'
 Has-Read $r 'consultar_solicitud'; No-Effects $r
 $r=Chat 'Comprobalo nuevamente y consulta tambien la disponibilidad actual.'
 Has-Read $r 'consultar_solicitud'; Has-Read $r 'disponibilidad'; No-Effects $r
 Write-Host 'APROBADO recorrido real por Spring: RAG, lecturas sucesivas, seleccion, conformidad y registro. Falta contrastar fila/auditoria SQL en pgAdmin y capturas de pagina.'
} catch {
 Write-Error $_.Exception.Message
 exit 1
}
