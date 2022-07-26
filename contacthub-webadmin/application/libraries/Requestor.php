<?php 
class Requestor
{
    public function curl_patch($url,$body = [],$header = []){
        $raw 	= $body;

		$ch = curl_init();
		curl_setopt( $ch, CURLOPT_URL, $url);
        curl_setopt( $ch, CURLOPT_POSTFIELDS, $raw);
        curl_setopt( $ch, CURLOPT_CUSTOMREQUEST, "PATCH");
		curl_setopt( $ch, CURLOPT_FOLLOWLOCATION, 1);
		curl_setopt( $ch, CURLOPT_HEADER, 0);
		curl_setopt( $ch, CURLOPT_HTTPHEADER, $header);
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1);

		$responseJson = curl_exec( $ch );

        $httpcode = curl_getinfo($ch,CURLINFO_HTTP_CODE);
        curl_close($ch);
        return $this->responseCurl = ['http_code'=>$httpcode, 'result'=> $responseJson];
    }
    public function curl_update($url,$body = [],$header = []){
        $raw 	= $body;

		$ch = curl_init();
		curl_setopt( $ch, CURLOPT_URL, $url);
        curl_setopt( $ch, CURLOPT_POSTFIELDS, $raw);
        curl_setopt( $ch, CURLOPT_CUSTOMREQUEST, "PUT");
		curl_setopt( $ch, CURLOPT_FOLLOWLOCATION, 1);
		curl_setopt( $ch, CURLOPT_HEADER, 0);
		curl_setopt( $ch, CURLOPT_HTTPHEADER, $header);
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1);

		$responseJson = curl_exec( $ch );

        $httpcode = curl_getinfo($ch,CURLINFO_HTTP_CODE);
        curl_close($ch);
        return $this->responseCurl = ['http_code'=>$httpcode, 'result'=> $responseJson];
    }
    public function curl_post($url,$body = [],$header = []){
        $raw 	= $body;

		$ch = curl_init();
		curl_setopt( $ch, CURLOPT_URL, $url);
        curl_setopt( $ch, CURLOPT_POSTFIELDS, $raw);
        curl_setopt( $ch, CURLOPT_CUSTOMREQUEST, "POST");
		curl_setopt( $ch, CURLOPT_FOLLOWLOCATION, 1);
		curl_setopt( $ch, CURLOPT_HEADER, 0);
		curl_setopt( $ch, CURLOPT_HTTPHEADER, $header);
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1);

		$responseJson = curl_exec( $ch );

        $httpcode = curl_getinfo($ch,CURLINFO_HTTP_CODE);
        curl_close($ch);
        return $this->responseCurl = ['http_code'=>$httpcode, 'result'=> $responseJson];
    }
    public function curl_get($url,$params=[],$body = [],$header = []){
        $raw 	= $body;
        $url .= http_build_query($params);
		$ch = curl_init();
		curl_setopt( $ch, CURLOPT_URL, $url);
        curl_setopt( $ch, CURLOPT_POSTFIELDS, $raw);
        curl_setopt( $ch, CURLOPT_CUSTOMREQUEST, "GET");
		curl_setopt( $ch, CURLOPT_FOLLOWLOCATION, 1);
		curl_setopt( $ch, CURLOPT_HEADER, 0);
		curl_setopt( $ch, CURLOPT_HTTPHEADER, $header);
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1);

		$responseJson = curl_exec( $ch );

        $httpcode = curl_getinfo($ch,CURLINFO_HTTP_CODE);
        curl_close($ch);
        return $this->responseCurl = ['http_code'=>$httpcode, 'result'=> $responseJson];
    }
    public function curl_delete($url,$params=[],$body = [],$header = []){
        $raw 	= $body;
        $url .= http_build_query($params);
		$ch = curl_init();
		curl_setopt( $ch, CURLOPT_URL, $url);
        curl_setopt( $ch, CURLOPT_POSTFIELDS, $raw);
        curl_setopt( $ch, CURLOPT_CUSTOMREQUEST, "DELETE");
		curl_setopt( $ch, CURLOPT_FOLLOWLOCATION, 1);
		curl_setopt( $ch, CURLOPT_HEADER, 0);
		curl_setopt( $ch, CURLOPT_HTTPHEADER, $header);
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1);

		$responseJson = curl_exec( $ch );

        $httpcode = curl_getinfo($ch,CURLINFO_HTTP_CODE);
        curl_close($ch);
        return $this->responseCurl = ['http_code'=>$httpcode, 'result'=> $responseJson];
    }
}
