<?php
require APPPATH.'vendor/autoload.php';
use GuzzleHttp\Client;

class Api_dashboard
{
    public function __construct(){
        $this->app =& get_instance();

        $this->client = new Client([
            'base_uri' => $this->app->config->item('BASE_URI_API'),
            'timeout'  => 120.0,
        ]);
    }
    
    public function get_summary(){
        try {
            $response   = $this->client->request('GET', '/dashboard');
            $result     = json_decode($response->getBody()->getContents());
        }catch (GuzzleHttp\Exception\BadResponseException $e) {
            $response = $e->getResponse();
            $result     = json_decode($response->getBody()->getContents());
        }
        return $result;
    }

}
