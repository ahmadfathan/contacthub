<?php
require APPPATH.'vendor/autoload.php';

use GuzzleHttp\Client;

class Api_city
{
    public function __construct(){
        $this->app =& get_instance();
        $this->client_gateway = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API_GATEWAY'),
            'timeout'  => 120.0,
        ]);
        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);
    }
    public function getAll($filter=[]){
        try {
            $response   = $this->client->request('GET', '/city',[
                'headers'=> [
                    'Content-Type' => 'application/json',
                ],
                'json' => $filter 
            ]);
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }
}
